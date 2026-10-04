package com.amansia.amansiabackend.dto.commande;

import jakarta.validation.constraints.NotNull;

public class CommandeCreationDTO {
    @NotNull(message = "L'adresse de livraison est obligatoire")
    private Integer adresseLivraisonId;
    private String commentaire;

    public Integer getAdresseLivraisonId() { return adresseLivraisonId; }
    public void setAdresseLivraisonId(Integer adresseLivraisonId) { this.adresseLivraisonId = adresseLivraisonId; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
