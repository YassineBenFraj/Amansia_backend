package com.amansia.amansiabackend.exception;

public class CodeHexadecimalInvalideException extends RuntimeException {

    public CodeHexadecimalInvalideException() {
        super("Le code hexadecimal doit respecter le format #RRGGBB");
    }
}
