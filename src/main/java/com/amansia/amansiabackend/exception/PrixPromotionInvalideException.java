package com.amansia.amansiabackend.exception;

public class PrixPromotionInvalideException extends RuntimeException {

    public PrixPromotionInvalideException() {
        super("Le prix promotionnel doit etre inferieur ou egal au prix normal");
    }
}
