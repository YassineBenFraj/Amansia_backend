package com.amansia.amansiabackend.dto.image;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ImageProduitUpdateDTO {

    @NotNull
    private Boolean principale;

    @NotNull
    @Min(0)
    private Integer ordre;

    public Boolean getPrincipale() {
        return principale;
    }

    public void setPrincipale(Boolean principale) {
        this.principale = principale;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }
}
