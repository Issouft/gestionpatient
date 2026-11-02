package com.forceN.gestionpatient.commun.exception;

import org.springframework.http.HttpStatus;

/**
 * Ce que toute erreur de l'application doit fournir.
 * Chaque domaine l'implémente avec son propre enum.
 */
public interface CodeErreur {

    /** Le nom de la constante de l'enum, renvoyé au client dans le champ "code". */
    String name();

    HttpStatus getStatut();

    String getTitre();
}
