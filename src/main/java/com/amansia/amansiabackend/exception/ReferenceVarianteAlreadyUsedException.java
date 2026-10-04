package com.amansia.amansiabackend.exception;

public class ReferenceVarianteAlreadyUsedException extends RuntimeException {

    public ReferenceVarianteAlreadyUsedException() {
        super("La reference de la variante est deja utilisee");
    }
}
