package com.amansia.amansiabackend.exception;

public class ImageProduitNotFoundException extends RuntimeException {

    public ImageProduitNotFoundException(Integer id) {
        super("Image produit introuvable avec l'id " + id);
    }
}
