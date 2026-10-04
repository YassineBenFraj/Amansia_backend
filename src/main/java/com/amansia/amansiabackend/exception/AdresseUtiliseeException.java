package com.amansia.amansiabackend.exception;
public class AdresseUtiliseeException extends RuntimeException {
 public AdresseUtiliseeException(){super("Cette adresse est utilisee par une commande et ne peut plus etre modifiee ou supprimee. Creez une nouvelle adresse.");}
}
