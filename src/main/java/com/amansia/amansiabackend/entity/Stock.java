package com.amansia.amansiabackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Getter @Setter
public class Stock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false, unique = true)
    private VarianteProduit variante;
    @Column(nullable = false)
    private Integer quantite;
    @Column(name = "seuil_alerte", nullable = false)
    private Integer seuilAlerte;
    @Column(name = "date_mise_a_jour", nullable = false)
    private LocalDateTime dateMiseAJour;
}
