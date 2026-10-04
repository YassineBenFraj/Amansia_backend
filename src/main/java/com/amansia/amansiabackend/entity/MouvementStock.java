package com.amansia.amansiabackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_stocks")
@Getter @Setter
public class MouvementStock {
    public enum Type { ENTREE, SORTIE, RETOUR, AJUSTEMENT }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variante_id", nullable = false)
    private VarianteProduit variante;
    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false)
    private Type typeMouvement;
    @Column(nullable = false)
    private Integer quantite;
    @Column(length = 255)
    private String motif;
    @Column(length = 100)
    private String reference;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement;
}
