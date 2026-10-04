package com.amansia.amansiabackend.dto.paiement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public class StripeFinalisationDTO {
    @NotBlank private String paymentIntentId;
    @NotNull private Integer adresseLivraisonId;
    private String commentaire;
    public String getPaymentIntentId(){return paymentIntentId;} public void setPaymentIntentId(String v){paymentIntentId=v;}
    public Integer getAdresseLivraisonId(){return adresseLivraisonId;} public void setAdresseLivraisonId(Integer v){adresseLivraisonId=v;}
    public String getCommentaire(){return commentaire;} public void setCommentaire(String v){commentaire=v;}
}
