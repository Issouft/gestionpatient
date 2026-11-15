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
    // Sert aux deux liens envoyés par email : invitation et mot de passe oublié.
    JETON_INVALIDE(HttpStatus.BAD_REQUEST, "Lien invalide ou expiré"),

    // Établissements (SaaS)
    // Le personnel est connecté, mais n'a pas encore choisi dans quel établissement il travaille.
    ETABLISSEMENT_NON_CHOISI(HttpStatus.FORBIDDEN, "Établissement non choisi"),
    // Même réponse si l'établissement n'existe pas ou si la personne n'y est pas affiliée : on ne révèle pas
    // quels établissements existent.
    ETABLISSEMENT_NON_AUTORISE(HttpStatus.FORBIDDEN, "Accès à l'établissement refusé"),
    // Le jeton désigne un établissement où la personne n'a plus accès (affiliation suspendue, établissement désactivé).
    ACCES_ETABLISSEMENT_RETIRE(HttpStatus.UNAUTHORIZED, "Accès à l'établissement retiré");

    private final HttpStatus statut;
    private final String titre;
}
