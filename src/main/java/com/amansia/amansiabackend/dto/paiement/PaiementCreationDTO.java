package com.amansia.amansiabackend.dto.paiement;

import com.amansia.amansiabackend.entity.MethodePaiement;
import jakarta.validation.constraints.NotNull;

public class PaiementCreationDTO {

    @NotNull(message = "La commande est obligatoire")
    private Integer commandeId;

    @NotNull(message = "La méthode de paiement est obligatoire")
    private MethodePaiement methode;

    public Integer getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Integer commandeId) {
        this.commandeId = commandeId;
    }

    public MethodePaiement getMethode() {
        return methode;
    }

    public void setMethode(MethodePaiement methode) {
        this.methode = methode;
    }
}
