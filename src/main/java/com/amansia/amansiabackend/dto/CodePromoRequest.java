package com.amansia.amansiabackend.dto;

import com.amansia.amansiabackend.entity.TypeReduction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CodePromoRequest(String code, TypeReduction typeReduction, BigDecimal valeur,
                               BigDecimal montantMinimum, LocalDateTime dateDebut, LocalDateTime dateFin,
                               Integer nombreUtilisationsMax, Integer nombreUtilisationsParClient, Boolean actif) {}
