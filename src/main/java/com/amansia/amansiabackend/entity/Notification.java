package com.amansia.amansiabackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", uniqueConstraints = @UniqueConstraint(name = "uq_notification_event", columnNames = {"utilisateur_id", "evenement_cle"}),
        indexes = @Index(name = "idx_notification_user_date", columnList = "utilisateur_id,date_creation"))
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Utilisateur utilisateur;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40)
    private TypeNotification type;
    @Column(nullable = false, length = 160)
    private String titre;
    @Column(nullable = false, length = 500)
    private String message;
    @Column(length = 255)
    private String lien;
    @Column(name = "est_lue", nullable = false)
    private Boolean estLue = false;
    @Column(name = "evenement_cle", nullable = false, length = 160)
    private String evenementCle;
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    @PrePersist void onCreate() { if (dateCreation == null) dateCreation = LocalDateTime.now(); }
    public Integer getId() { return id; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public TypeNotification getType() { return type; }
    public String getTitre() { return titre; }
    public String getMessage() { return message; }
    public String getLien() { return lien; }
    public Boolean getEstLue() { return estLue; }
    public String getEvenementCle() { return evenementCle; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setUtilisateur(Utilisateur value) { utilisateur = value; }
    public void setType(TypeNotification value) { type = value; }
    public void setTitre(String value) { titre = value; }
    public void setMessage(String value) { message = value; }
    public void setLien(String value) { lien = value; }
    public void setEstLue(Boolean value) { estLue = value; }
    public void setEvenementCle(String value) { evenementCle = value; }
}
