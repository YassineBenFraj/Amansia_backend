package com.amansia.amansiabackend.dto.commande;

import jakarta.validation.constraints.NotNull;

public class CommandeCreationDTO {
    @NotNull(message = "L'adresse de livraison est obligatoire")
    private Integer adresseLivraisonId;
    private String commentaire;
    private String codePromo;

    public Integer getAdresseLivraisonId() { return adresseLivraisonId; }
    public void setAdresseLivraisonId(Integer adresseLivraisonId) { this.adresseLivraisonId = adresseLivraisonId; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public String getCodePromo() { return codePromo; }
    public void setCodePromo(String codePromo) { this.codePromo = codePromo; }
}
