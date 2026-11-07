package com.forceN.gestionpatient.securite.exception;

import com.forceN.gestionpatient.commun.exception.CodeErreur;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SecuriteErreur implements CodeErreur {
    IDENTIFIANTS_INVALIDES(HttpStatus.UNAUTHORIZED, "Identifiants invalides"),
    NON_AUTHENTIFIE(HttpStatus.UNAUTHORIZED, "Authentification requise"),
    ACCES_REFUSE(HttpStatus.FORBIDDEN, "Accès refusé"),
    // Un seul message pour jeton inconnu, expiré ou déjà utilisé : on ne dit pas à un attaquant lequel.
    JETON_INVALIDE(HttpStatus.BAD_REQUEST, "Lien d'activation invalide ou expiré");
    ;


    private final HttpStatus statut;
    private final String titre;
}
