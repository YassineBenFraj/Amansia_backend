package com.amansia.amansiabackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(com.amansia.amansiabackend.auth.service.LienReinitialisationException.class)
    public ResponseEntity<ErrorResponse> lienReinitialisation(com.amansia.amansiabackend.auth.service.LienReinitialisationException e) {
        return response(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(com.amansia.amansiabackend.auth.service.EmailIndisponibleException.class)
    public ResponseEntity<ErrorResponse> emailIndisponible(com.amansia.amansiabackend.auth.service.EmailIndisponibleException e) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }
    @ExceptionHandler(AdresseNotFoundException.class)
    public ResponseEntity<ErrorResponse> adresseAbsente(AdresseNotFoundException e) { return response(HttpStatus.NOT_FOUND,e.getMessage()); }
    @ExceptionHandler(AdresseUtiliseeException.class)
    public ResponseEntity<ErrorResponse> adresseUtilisee(AdresseUtiliseeException e) { return response(HttpStatus.CONFLICT,e.getMessage()); }


    @ExceptionHandler(UtilisateurNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UtilisateurNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleConflict(EmailAlreadyUsedException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(CategorieNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategorieNotFound(CategorieNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(CategorieNameAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleCategorieNameConflict(CategorieNameAlreadyUsedException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ProduitNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProduitNotFound(ProduitNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({CategorieInactiveException.class, PrixPromotionInvalideException.class})
    public ResponseEntity<ErrorResponse> handleProduitValidation(RuntimeException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MateriauNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMateriauNotFound(MateriauNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(MateriauNameAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleMateriauNameConflict(MateriauNameAlreadyUsedException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(CouleurNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCouleurNotFound(CouleurNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(CouleurNameAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleCouleurNameConflict(CouleurNameAlreadyUsedException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(CodeHexadecimalInvalideException.class)
    public ResponseEntity<ErrorResponse> handleCodeHexadecimalInvalide(CodeHexadecimalInvalideException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(VarianteProduitNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVarianteProduitNotFound(VarianteProduitNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(PanierNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePanierNotFound(PanierNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(LignePanierNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLignePanierNotFound(LignePanierNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ReferenceVarianteAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleReferenceVarianteConflict(ReferenceVarianteAlreadyUsedException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler({MateriauInactiveException.class, CouleurInactiveException.class, QuantiteInvalideException.class,
            VarianteProduitInvalideException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBusinessValidation(Exception exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(PanierAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handlePanierAccessDenied(PanierAccessDeniedException exception) {
        return response(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(CommandeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommandeNotFound(CommandeNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(CommandeAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleCommandeAccessDenied(CommandeAccessDeniedException exception) {
        return response(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(CommandeAnnulationImpossibleException.class)
    public ResponseEntity<ErrorResponse> handleCommandeAnnulationImpossible(CommandeAnnulationImpossibleException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({PaiementNotFoundException.class, AvisNotFoundException.class})
    public ResponseEntity<ErrorResponse> handlePaiementOrAvisNotFound(RuntimeException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({PaiementAlreadyExistsException.class, AvisAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handlePaymentOrReviewConflict(RuntimeException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(AvisAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAvisAccessDenied(AvisAccessDeniedException exception) {
        return response(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(StripeWebhookInvalidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStripeWebhook(StripeWebhookInvalidException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(StripeConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleStripeConfiguration(StripeConfigurationException exception) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }

    @ExceptionHandler(StripeRequestException.class)
    public ResponseEntity<ErrorResponse> handleStripeRequest(StripeRequestException exception) {
        return response(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }

    @ExceptionHandler(ImageProduitNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleImageNotFound(ImageProduitNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({FichierImageInvalideException.class, ProduitInactiveException.class,
            MaxUploadSizeExceededException.class})
    public ResponseEntity<ErrorResponse> handleImageValidation(Exception exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ResponseEntity<ErrorResponse> handleCredentials(Exception exception) {
        return response(HttpStatus.UNAUTHORIZED, "Identifiants incorrects");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return response(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message));
    }
}
