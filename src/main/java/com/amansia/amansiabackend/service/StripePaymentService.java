package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.entity.Paiement;
import com.amansia.amansiabackend.entity.StatutPaiement;
import com.amansia.amansiabackend.entity.StatutCommande;
import com.amansia.amansiabackend.entity.MethodePaiement;
import com.amansia.amansiabackend.exception.StripeConfigurationException;
import com.amansia.amansiabackend.exception.StripeRequestException;
import com.amansia.amansiabackend.exception.StripeWebhookInvalidException;
import com.amansia.amansiabackend.repository.PaiementRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Locale;

@Service
public class StripePaymentService {
 private static final Logger log = LoggerFactory.getLogger(StripePaymentService.class);
 private final PaiementRepository paiementRepository;
 private final com.amansia.amansiabackend.repository.CommandeRepository commandeRepository;
 private final PanierCommandeService panierCommandeService;
 public StripePaymentService(PaiementRepository p, com.amansia.amansiabackend.repository.CommandeRepository commandes, PanierCommandeService panierCommandeService){ this.paiementRepository=p; this.commandeRepository=commandes; this.panierCommandeService=panierCommandeService; }
 public String creerCheckoutSession(Object ignored){ throw new UnsupportedOperationException("Stripe Checkout externe désactivé"); }
 @Value("${stripe.secret-key:}") private String secretKey;
 @Value("${stripe.publishable-key:}") private String publishableKey;
 @Value("${stripe.webhook-secret:}") private String webhookSecret;
 @Value("${stripe.currency:tnd}") private String currency;
 @Value("${stripe.tnd-to-eur-rate:}") private String tndToEurRate;
 public String creerPaymentIntent(Paiement paiement) {
  return creerPaymentIntent(paiement.getMontant(), "Panier Amansia", paiement.getReference(), paiement.getCommande().getUtilisateur().getId());
 }
 public String creerPaymentIntent(BigDecimal montant,String description,String reference,Integer utilisateurId) {
  verifierCle();
  try {
    String normalizedCurrency = currency == null ? "" : currency.trim().toLowerCase(Locale.ROOT);
    long amount = convertir(montant, normalizedCurrency);
    PaymentIntentCreateParams params=PaymentIntentCreateParams.builder().setAmount(amount).setCurrency(normalizedCurrency).setDescription(description).putMetadata("payment_reference",reference).putMetadata("utilisateur_id",String.valueOf(utilisateurId)).setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()).build();
   PaymentIntent intent=PaymentIntent.create(params, RequestOptions.builder().setApiKey(secretKey).build());
   return intent.getClientSecret();
  } catch(StripeException e){
    log.error("Stripe PaymentIntent creation failed: code={}, status={}, requestId={}, message={}", e.getCode(), e.getStatusCode(), e.getRequestId(), e.getMessage());
    throw new StripeRequestException("Impossible de créer le paiement Stripe",e);
  } catch(ArithmeticException|IllegalArgumentException e){
    throw new StripeRequestException("Montant ou devise Stripe invalide",e);
  }
 }
 public String preparerAutorisation(BigDecimal montant, String reference, Integer utilisateurId) {
  verifierCle();
  try {
   String devise=currency.trim().toLowerCase(Locale.ROOT);
   PaymentIntentCreateParams params=PaymentIntentCreateParams.builder()
     .setAmount(convertir(montant,devise)).setCurrency(devise)
     .setDescription("Panier Amansia")
     .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
     .putMetadata("payment_reference",reference)
     .putMetadata("utilisateur_id",String.valueOf(utilisateurId))
     .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
     .build();
   return PaymentIntent.create(params,RequestOptions.builder().setApiKey(secretKey).build()).getClientSecret();
  } catch(StripeException e) { throw new StripeRequestException("Impossible de préparer le paiement Stripe",e); }
 }
 public void capturerPreparation(String paymentIntentId) {
  verifierCle();
  try {
   RequestOptions options=RequestOptions.builder().setApiKey(secretKey)
     .setIdempotencyKey("amansia-capture-"+paymentIntentId).build();
   PaymentIntent intent=PaymentIntent.retrieve(paymentIntentId,options);
   if ("succeeded".equals(intent.getStatus())) return;
   if (!"requires_capture".equals(intent.getStatus()) || !"succeeded".equals(intent.capture(options).getStatus()))
    throw new StripeWebhookInvalidException("La capture Stripe n'a pas abouti");
  } catch(StripeException e) { throw new StripeRequestException("Impossible de capturer le paiement Stripe",e); }
 }
 public PaymentIntent verifierPreparation(String paymentIntentId,Integer utilisateurId,BigDecimal montant){
  verifierCle();
  try{
   PaymentIntent intent=PaymentIntent.retrieve(paymentIntentId,RequestOptions.builder().setApiKey(secretKey).build());
   String devise=currency.trim().toLowerCase(Locale.ROOT);
   if(!"requires_capture".equals(intent.getStatus()) || !String.valueOf(utilisateurId).equals(intent.getMetadata().get("utilisateur_id"))
       || intent.getAmount()!=convertir(montant,devise) || !devise.equalsIgnoreCase(intent.getCurrency()))
    throw new StripeWebhookInvalidException("Le paiement Stripe ne correspond pas au panier");
   return intent;
  }catch(StripeException e){throw new StripeRequestException("Impossible de vérifier le paiement Stripe",e);}
 }
 @Transactional public void traiterWebhook(String payload,String signature){
  if(webhookSecret==null||webhookSecret.isBlank()) throw new StripeConfigurationException("STRIPE_WEBHOOK_SECRET non configurÃ©e");
  try { Webhook.constructEvent(payload,signature,webhookSecret); } catch(SignatureVerificationException e){ throw new StripeWebhookInvalidException("Signature Stripe invalide",e); }
  JsonObject root=JsonParser.parseString(payload).getAsJsonObject(); String type=root.get("type").getAsString(); if(!type.startsWith("payment_intent.")) return;
  JsonObject obj=root.getAsJsonObject("data").getAsJsonObject("object"); String ref=obj.getAsJsonObject("metadata").get("payment_reference").getAsString();
  Integer commandeId=paiementRepository.findCommandeIdByReference(ref).orElse(null); if(commandeId==null) return;
  commandeRepository.findByIdForUpdate(commandeId).orElseThrow();
  Paiement p=paiementRepository.findByReferenceForUpdate(ref).orElse(null); if(p==null) return;
  if("payment_intent.succeeded".equals(type)){
   String devise=currency.trim().toLowerCase(Locale.ROOT);
   if(p.getMethode()!=MethodePaiement.CARTE || !obj.has("amount") || obj.get("amount").getAsLong()!=convertir(p.getMontant(),devise)
       || !obj.has("currency") || !devise.equalsIgnoreCase(obj.get("currency").getAsString())
       || !obj.has("status") || !"succeeded".equals(obj.get("status").getAsString())) throw new StripeWebhookInvalidException("Paiement Stripe incompatible");
   confirmerPaiement(p);
  }
  else if("payment_intent.payment_failed".equals(type)&&p.getStatut()==StatutPaiement.EN_ATTENTE){p.setStatut(StatutPaiement.ECHOUE);paiementRepository.save(p);}
 }
 private long convertir(BigDecimal montant, String normalizedCurrency){
  if(montant == null || montant.signum() <= 0) throw new IllegalArgumentException("Le montant Stripe doit être positif");
  if(normalizedCurrency.isBlank()) throw new IllegalArgumentException("La devise Stripe est obligatoire");
  if("eur".equals(normalizedCurrency)) montant = convertirTndEnEur(montant);
  int d=Currency.getInstance(normalizedCurrency.toUpperCase(Locale.ROOT)).getDefaultFractionDigits();
  long amount=montant.movePointRight(d).longValueExact();
  if(amount <= 0) throw new IllegalArgumentException("Le montant Stripe doit être positif");
  return amount;
 }
 private BigDecimal convertirTndEnEur(BigDecimal montantTnd){
  if(tndToEurRate == null || tndToEurRate.isBlank()) throw new IllegalArgumentException("STRIPE_TND_TO_EUR_RATE est obligatoire quand Stripe utilise EUR");
  BigDecimal rate = new BigDecimal(tndToEurRate.trim());
  if(rate.signum() <= 0) throw new IllegalArgumentException("STRIPE_TND_TO_EUR_RATE doit être positif");
  return montantTnd.multiply(rate).setScale(2, RoundingMode.HALF_UP);
 }
 private void verifierCle(){if(secretKey==null||secretKey.isBlank())throw new StripeConfigurationException("STRIPE_SECRET_KEY non configurÃ©e");}
 public void verifierPaymentIntent(Paiement paiement,String paymentIntentId){
  verifierCle();
  if(paiement.getMethode()!=MethodePaiement.CARTE) throw new IllegalArgumentException("Ce paiement n'est pas un paiement par carte");
  try{
   PaymentIntent intent=PaymentIntent.retrieve(paymentIntentId,RequestOptions.builder().setApiKey(secretKey).build());
   String reference=intent.getMetadata().get("payment_reference");
  String normalizedCurrency = currency == null ? "" : currency.trim().toLowerCase(Locale.ROOT);
  if(!paiement.getReference().equals(reference)||intent.getAmount()!=convertir(paiement.getMontant(), normalizedCurrency)||!normalizedCurrency.equalsIgnoreCase(intent.getCurrency())) throw new StripeWebhookInvalidException("Le PaymentIntent ne correspond pas au paiement attendu");
   if(!"succeeded".equals(intent.getStatus())) throw new IllegalArgumentException("Le paiement Stripe n'est pas confirmé");
   confirmerPaiement(paiement);
  }catch(StripeException e){throw new StripeRequestException("Impossible de vérifier le paiement Stripe",e);}
 }
 private void confirmerPaiement(Paiement paiement){
  var verrou = commandeRepository.findByIdForUpdate(paiement.getCommande().getId()).orElseThrow();
  paiement = paiementRepository.findByReferenceForUpdate(paiement.getReference()).orElseThrow();
  if(paiement.getStatut()==StatutPaiement.PAYE) return;
  if(paiement.getStatut()==StatutPaiement.REMBOURSE || verrou.getStatut()!=StatutCommande.EN_ATTENTE) throw new IllegalArgumentException("Commande incompatible avec la confirmation du paiement");
  paiement.setStatut(StatutPaiement.PAYE); paiement.setDatePaiement(LocalDateTime.now()); paiementRepository.save(paiement);
  var commande=paiement.getCommande();
  if(commande!=null&&commande.getStatut()==StatutCommande.EN_ATTENTE){commande.setStatut(StatutCommande.CONFIRMEE);commandeRepository.save(commande);panierCommandeService.retirerArticlesCommandes(commande);}
 }
 public String getPublishableKey(){
  if(publishableKey==null||publishableKey.isBlank()) throw new StripeConfigurationException("STRIPE_PUBLISHABLE_KEY non configurée");
  return publishableKey;
 }
}
