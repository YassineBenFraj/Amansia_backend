package com.amansia.amansiabackend.dto.panier;

import java.math.BigDecimal;
import java.util.List;

public class PanierDTO {

    private Integer id;
    private List<LignePanierDTO> lignes;
    private BigDecimal totalGeneral;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<LignePanierDTO> getLignes() {
        return lignes;
    }

    public void setLignes(List<LignePanierDTO> lignes) {
        this.lignes = lignes;
    }

    public BigDecimal getTotalGeneral() {
        return totalGeneral;
    }

    public void setTotalGeneral(BigDecimal totalGeneral) {
        this.totalGeneral = totalGeneral;
    }
}
