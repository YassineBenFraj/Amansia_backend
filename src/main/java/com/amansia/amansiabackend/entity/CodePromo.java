package com.amansia.amansiabackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "codes_promo")
@Getter @Setter
public class CodePromo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true, length = 40)
    private String code;
    @Enumerated(EnumType.STRING)
    @Column(name = "type_reduction", nullable = false)
    private TypeReduction typeReduction;
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal valeur;
    @Column(name = "montant_minimum", nullable = false, precision = 12, scale = 3)
    private BigDecimal montantMinimum;
    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;
    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;
    @Column(name = "nombre_utilisations_max")
    private Integer nombreUtilisationsMax;
    @Column(name = "nombre_utilisations_par_client")
    private Integer nombreUtilisationsParClient;
    @Column(name = "nombre_utilisations", nullable = false)
    private Integer nombreUtilisations = 0;
    @Column(nullable = false)
    private Boolean actif = true;
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    @PrePersist void created() { if (dateCreation == null) dateCreation = LocalDateTime.now(); }
}
