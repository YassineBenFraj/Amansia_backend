package com.amansia.amansiabackend.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity @Table(name="adresses") @Getter @Setter
public class Adresse {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="utilisateur_id",nullable=false) private Utilisateur utilisateur;
 @Column(nullable=false,length=255) private String adresse;
 @Column(nullable=false,length=100) private String ville;
 @Column(length=100) private String gouvernorat;
 @Column(name="code_postal",length=20) private String codePostal;
 @Column(length=100) private String pays;
 @Column(name="adresse_principale",nullable=false) private Boolean adressePrincipale;
 @Column(name="date_creation",nullable=false,updatable=false) private LocalDateTime dateCreation;
 @PrePersist void initialiser(){dateCreation=LocalDateTime.now();if(pays==null)pays="Tunisie";if(adressePrincipale==null)adressePrincipale=false;}
}
