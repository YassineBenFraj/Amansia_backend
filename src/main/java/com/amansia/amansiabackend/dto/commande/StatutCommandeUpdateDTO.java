package com.amansia.amansiabackend.dto.commande;
import com.amansia.amansiabackend.entity.StatutCommande;
import jakarta.validation.constraints.NotNull;
public record StatutCommandeUpdateDTO(@NotNull StatutCommande statut) {}
