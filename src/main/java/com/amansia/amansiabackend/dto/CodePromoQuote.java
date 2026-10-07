package com.amansia.amansiabackend.dto;

import java.math.BigDecimal;

public record CodePromoQuote(Integer codePromoId, String code, BigDecimal sousTotal, BigDecimal reduction, BigDecimal total) {}
