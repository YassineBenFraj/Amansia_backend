package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.*;
import com.amansia.amansiabackend.entity.*;
import com.amansia.amansiabackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CodePromoService {
    private static final Pattern FORMAT = Pattern.compile("[A-Z0-9][A-Z0-9_-]{2,39}");
    private final CodePromoRepository codes;
    private final UtilisationCodePromoRepository utilisations;

    @Transactional(readOnly = true)
    public List<CodePromoDTO> list() {
        return codes.findAll(Sort.by(Sort.Direction.DESC, "dateCreation")).stream().map(this::dto).toList();
    }

    @Transactional
    public CodePromoDTO save(Integer id, CodePromoRequest request) {
        if (request == null) throw new IllegalArgumentException("Les informations du code sont obligatoires.");
        String normalized = normalize(request.code());
        if (!FORMAT.matcher(normalized).matches()) throw new IllegalArgumentException("Le code doit contenir 3 à 40 caractères alphanumériques.");
        if (request.typeReduction() == null || request.valeur() == null || request.valeur().signum() <= 0
                || (request.typeReduction() == TypeReduction.POURCENTAGE && request.valeur().compareTo(new BigDecimal("100")) > 0))
            throw new IllegalArgumentException("La réduction doit être positive et le pourcentage ne peut pas dépasser 100 %.");
        if (request.montantMinimum() == null || request.montantMinimum().signum() < 0)
            throw new IllegalArgumentException("Le montant minimum est invalide.");
        if (request.dateDebut() == null || request.dateFin() == null || !request.dateFin().isAfter(request.dateDebut()))
            throw new IllegalArgumentException("La date de fin doit suivre la date de début.");
        if ((request.nombreUtilisationsMax() != null && request.nombreUtilisationsMax() < 1)
                || (request.nombreUtilisationsParClient() != null && request.nombreUtilisationsParClient() < 1))
            throw new IllegalArgumentException("Les limites d'utilisation doivent être positives.");
        CodePromo code = id == null ? new CodePromo() : codes.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Code promo introuvable."));
        codes.findByCode(normalized).ifPresent(existing -> {
            if (!existing.getId().equals(code.getId())) throw new IllegalArgumentException("Ce code promo existe déjà.");
        });
        if (request.nombreUtilisationsMax() != null && request.nombreUtilisationsMax() < code.getNombreUtilisations())
            throw new IllegalArgumentException("La nouvelle limite est inférieure aux utilisations déjà enregistrées.");
        code.setCode(normalized);
        code.setTypeReduction(request.typeReduction()); code.setValeur(request.valeur());
        code.setMontantMinimum(request.montantMinimum()); code.setDateDebut(request.dateDebut());
        code.setDateFin(request.dateFin()); code.setNombreUtilisationsMax(request.nombreUtilisationsMax());
        code.setNombreUtilisationsParClient(request.nombreUtilisationsParClient());
        code.setActif(Boolean.TRUE.equals(request.actif()));
        return dto(codes.save(code));
    }

    @Transactional
    public CodePromoDTO status(Integer id, boolean active) {
        CodePromo code = codes.findById(id).orElseThrow(() -> new IllegalArgumentException("Code promo introuvable."));
        code.setActif(active);
        return dto(codes.save(code));
    }

    @Transactional(readOnly = true)
    public CodePromoQuote quote(String enteredCode, Integer userId, BigDecimal subtotal) {
        if (enteredCode == null || enteredCode.isBlank()) return new CodePromoQuote(null, null, subtotal, BigDecimal.ZERO, subtotal);
        CodePromo code = codes.findByCode(normalize(enteredCode))
                .orElseThrow(() -> new IllegalArgumentException("Ce code promo n'existe pas."));
        return calculate(code, userId, subtotal, false);
    }

    @Transactional
    public CodePromoQuote lockedQuote(String enteredCode, Integer userId, BigDecimal subtotal) {
        if (enteredCode == null || enteredCode.isBlank()) return new CodePromoQuote(null, null, subtotal, BigDecimal.ZERO, subtotal);
        CodePromo code = codes.lockByCode(normalize(enteredCode))
                .orElseThrow(() -> new IllegalArgumentException("Ce code promo n'existe pas."));
        return calculate(code, userId, subtotal, true);
    }

    @Transactional
    public void consume(Commande order, Integer userId, BigDecimal discount) {
        if (order.getCodePromo() == null) return;
        if (utilisations.existsByCommandeId(order.getId())) return;
        CodePromo code = codes.lockByCode(order.getCodePromo()).orElseThrow();
        if (code.getId().equals(order.getCodePromoId()) && calculate(code, userId, order.getSousTotal(), true).reduction().compareTo(discount) == 0) {
            UtilisationCodePromo usage = new UtilisationCodePromo();
            usage.setCodePromo(code); usage.setUtilisateurId(userId); usage.setCommande(order);
            usage.setMontantReduction(discount);
            utilisations.saveAndFlush(usage);
            code.setNombreUtilisations(code.getNombreUtilisations() + 1);
            codes.save(code);
        } else throw new IllegalArgumentException("Le code promo a changé pendant le paiement.");
    }

    private CodePromoQuote calculate(CodePromo code, Integer userId, BigDecimal subtotal, boolean currentRead) {
        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(code.getActif())) throw new IllegalArgumentException("Ce code promo est désactivé.");
        if (now.isBefore(code.getDateDebut())) throw new IllegalArgumentException("Ce code promo n'est pas encore actif.");
        if (now.isAfter(code.getDateFin())) throw new IllegalArgumentException("Ce code promo a expiré.");
        if (code.getNombreUtilisationsMax() != null && code.getNombreUtilisations() >= code.getNombreUtilisationsMax())
            throw new IllegalArgumentException("Ce code promo a atteint sa limite d'utilisation.");
        long usedByClient = code.getNombreUtilisationsParClient() == null ? 0 : currentRead
                ? utilisations.findForClientForUpdate(code.getId(), userId).size()
                : utilisations.countByCodePromoIdAndUtilisateurId(code.getId(), userId);
        if (code.getNombreUtilisationsParClient() != null && usedByClient >= code.getNombreUtilisationsParClient())
            throw new IllegalArgumentException("Vous avez déjà utilisé ce code promo.");
        if (subtotal == null || subtotal.signum() <= 0) throw new IllegalArgumentException("Le panier est vide.");
        if (subtotal.compareTo(code.getMontantMinimum()) < 0)
            throw new IllegalArgumentException("Le montant minimum pour utiliser ce code est de " + code.getMontantMinimum() + " DT.");
        BigDecimal discount = code.getTypeReduction() == TypeReduction.POURCENTAGE
                ? subtotal.multiply(code.getValeur()).divide(new BigDecimal("100"), 3, RoundingMode.HALF_UP)
                : code.getValeur();
        discount = discount.min(subtotal);
        return new CodePromoQuote(code.getId(), code.getCode(), subtotal, discount, subtotal.subtract(discount));
    }

    private String normalize(String code) { return code == null ? "" : code.trim().toUpperCase(Locale.ROOT); }
    private CodePromoDTO dto(CodePromo c) {
        return new CodePromoDTO(c.getId(), c.getCode(), c.getTypeReduction(), c.getValeur(), c.getMontantMinimum(),
                c.getDateDebut(), c.getDateFin(), c.getNombreUtilisationsMax(), c.getNombreUtilisationsParClient(),
                c.getNombreUtilisations(), Boolean.TRUE.equals(c.getActif()), c.getDateCreation());
    }
}
