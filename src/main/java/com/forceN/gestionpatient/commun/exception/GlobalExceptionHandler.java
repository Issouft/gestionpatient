package com.forceN.gestionpatient.commun.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Transforme les exceptions en réponses JSON au format ProblemDetail.
 * La classe mère garde les bons statuts pour les erreurs de Spring (405, JSON illisible...).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ProblemDetail gererApplicationException(ApplicationException exception) {
        CodeErreur codeErreur = exception.getCodeErreur();
        return creerReponse(codeErreur.getStatut(), codeErreur.getTitre(), exception.getMessage(), codeErreur.name());
    }

    // Erreurs de @Valid : renvoie le message de chaque champ invalide, au lieu du message générique de Spring.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(erreur -> erreurs.putIfAbsent(erreur.getField(), erreur.getDefaultMessage()));

        ProblemDetail reponse = creerReponse(HttpStatus.BAD_REQUEST, "Données invalides",
                "Un ou plusieurs champs sont invalides.", "DONNEES_INVALIDES");
        reponse.setProperty("erreurs", erreurs);
        return ResponseEntity.badRequest().body(reponse);
    }

    // Relancées pour que Spring Security les traite, sinon elles deviendraient des erreurs 500.
    @ExceptionHandler({ AccessDeniedException.class, AuthenticationException.class })
    public void relancerErreurDeSecurite(Exception exception) throws Exception {
        throw exception;
    }

    // Le détail reste dans les logs : il ne doit jamais être envoyé au client.
    @ExceptionHandler(Exception.class)
    public ProblemDetail gererErreurImprevue(Exception exception) {
        log.error("Erreur imprévue", exception);
        return creerReponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne",
                "Une erreur inattendue est survenue.", "ERREUR_INTERNE");
    }

    private ProblemDetail creerReponse(HttpStatus statut, String titre, String detail, String code) {
        ProblemDetail reponse = ProblemDetail.forStatusAndDetail(statut, detail);
        reponse.setTitle(titre);
        reponse.setProperty("code", code);
        return reponse;
    }
}
