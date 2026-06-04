package cm.transport.service_clients_2.exception;


import cm.transport.service_clients_2.dto.ErreurApiDTO;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestControllerAdvice
@Hidden // @Hidden = cache ce controller de la documentation Swagger
public class GlobalExceptionHandler {

    // ERREURS DE VALIDATION

    /**
     * Gère les erreurs de validation @Valid.
     * Quand un DTO ne passe pas la validation (email invalide, champ vide...)
     * → 400 Bad Request avec la liste des erreurs par champ
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurApiDTO> gererErreurValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Collecte toutes les erreurs champ par champ
        Map<String, String> erreursParChamp = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            // FieldError = erreur sur un champ spécifique
            String nomChamp = ((FieldError) error).getField();
            String messageErreur = error.getDefaultMessage();
            erreursParChamp.put(nomChamp, messageErreur);
        });

        log.warn("Erreur de validation : {}", erreursParChamp);

        ErreurApiDTO erreur = ErreurApiDTO.builder()
                .timestamp(LocalDateTime.now())
                .statut(400)
                .erreur("Données invalides")
                .message("Veuillez corriger les erreurs de validation")
                .chemin(request.getRequestURI())
                .details(erreursParChamp)
                .build();

        return ResponseEntity.badRequest().body(erreur);
    }

    // ERREURS MÉTIER PERSONNALISÉES
    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErreurApiDTO> gererClientNonTrouve(
            ClientNotFoundException ex, HttpServletRequest request) {
        log.warn("Client non trouvé : {}", ex.getMessage());
        return creerReponseErreur(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(ClientDejaExistantException.class)
    public ResponseEntity<ErreurApiDTO> gererClientExistant(
            ClientDejaExistantException ex, HttpServletRequest request) {
        log.warn("Conflit client : {}", ex.getMessage());
        return creerReponseErreur(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    @ExceptionHandler(MotDePasseInvalideException.class)
    public ResponseEntity<ErreurApiDTO> gererMotDePasseInvalide(
            MotDePasseInvalideException ex, HttpServletRequest request) {
        return creerReponseErreur(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    }

    // ERREURS SPRING SECURITY

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErreurApiDTO> gererMauvaisIdentifiants(
            BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Mauvais identifiants pour : {}", request.getRequestURI());
        return creerReponseErreur(HttpStatus.UNAUTHORIZED, "Unauthorized",
                "Email ou mot de passe incorrect", request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErreurApiDTO> gererCompteDesactive(
            DisabledException ex, HttpServletRequest request) {
        return creerReponseErreur(HttpStatus.FORBIDDEN, "Forbidden",
                "Ce compte a été désactivé", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErreurApiDTO> gererAccesRefuse(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Accès refusé : {} {}", request.getMethod(), request.getRequestURI());
        return creerReponseErreur(HttpStatus.FORBIDDEN, "Forbidden",
                "Vous n'avez pas les droits pour accéder à cette ressource", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErreurApiDTO> gererErreurAuth(
            AuthenticationException ex, HttpServletRequest request) {
        return creerReponseErreur(HttpStatus.UNAUTHORIZED, "Unauthorized",
                "Authentification requise : " + ex.getMessage(), request);
    }

    // ERREUR GÉNÉRALE

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurApiDTO> gererErreurGenerale(
            Exception ex, HttpServletRequest request) {
        log.error("Erreur interne non gérée : ", ex);
        return creerReponseErreur(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Une erreur interne s'est produite. Contactez le support.",
                request);
    }

    // MÉTHODE UTILITAIRE

    private ResponseEntity<ErreurApiDTO> creerReponseErreur(
            HttpStatus status, String typeErreur, String message,
            HttpServletRequest request) {

        ErreurApiDTO erreur = ErreurApiDTO.builder()
                .timestamp(LocalDateTime.now())
                .statut(status.value())
                .erreur(typeErreur)
                .message(message)
                .chemin(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(erreur);
    }
}
