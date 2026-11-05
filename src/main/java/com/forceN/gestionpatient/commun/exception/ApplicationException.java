package com.forceN.gestionpatient.commun.exception;

import lombok.Getter;

/**
 * Exception mère de l'application. Une classe qui en hérite est prise en charge
 * par GlobalExceptionHandler sans avoir à le modifier.
 */
@Getter
public class ApplicationException extends RuntimeException {

    private final CodeErreur codeErreur;

    public ApplicationException(CodeErreur codeErreur, String message) {
        super(message);
        this.codeErreur = codeErreur;
    }
}
