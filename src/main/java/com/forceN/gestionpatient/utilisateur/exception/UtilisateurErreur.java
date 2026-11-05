package com.forceN.gestionpatient.utilisateur.exception;

import com.forceN.gestionpatient.commun.exception.CodeErreur;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UtilisateurErreur implements CodeErreur {
    // Ajouter les erreurs ici, avant le point-virgule. Exemple : NOM_ERREUR(HttpStatus.NOT_FOUND, "Titre de l'erreur");
    EMAIL_DEJA_UTILISE(HttpStatus.CONFLICT, "L'email est déjà utilisé"),
    UTILISATEUR_INTROUVABLE(HttpStatus.NOT_FOUND, "Utilisateur introuvable")
    ;

    private final HttpStatus statut;
    private final String titre;
}
