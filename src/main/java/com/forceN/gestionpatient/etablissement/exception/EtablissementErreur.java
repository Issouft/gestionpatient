package com.forceN.gestionpatient.etablissement.exception;

import com.forceN.gestionpatient.commun.exception.CodeErreur;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** Les erreurs du domaine etablissement. Exemple : NOM_ERREUR(HttpStatus.NOT_FOUND, "Titre de l'erreur"). */
@Getter
@RequiredArgsConstructor
public enum EtablissementErreur implements CodeErreur {
    ETABLISSEMENT_INTROUVABLE(HttpStatus.NOT_FOUND, "Établissement introuvable");

    private final HttpStatus statut;
    private final String titre;
}
