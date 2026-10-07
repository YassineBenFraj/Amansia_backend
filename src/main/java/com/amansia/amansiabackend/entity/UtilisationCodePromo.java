package com.amansia.amansiabackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisations_codes_promo")
@Getter @Setter
public class UtilisationCodePromo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "code_promo_id", nullable = false)
    private CodePromo codePromo;
    @Column(name = "utilisateur_id", nullable = false)
    private Integer utilisateurId;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_id", nullable = false, unique = true)
    private Commande commande;
    @Column(name = "date_utilisation", nullable = false)
    private LocalDateTime dateUtilisation;
    @Column(name = "montant_reduction", nullable = false, precision = 12, scale = 3)
    private BigDecimal montantReduction;
    @PrePersist void created() { if (dateUtilisation == null) dateUtilisation = LocalDateTime.now(); }
}
