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
    UTILISATEUR_INTROUVABLE(HttpStatus.NOT_FOUND, "Utilisateur introuvable"),
    // Cette route ne crée que du personnel : ni patient (il s'inscrit seul), ni administrateur.
    ROLE_NON_AUTORISE(HttpStatus.BAD_REQUEST, "Ce rôle ne peut pas être créé ici"),
    INFORMATIONS_MEDECIN_MANQUANTES(HttpStatus.BAD_REQUEST, "La spécialité et le numéro d'ordre sont obligatoires pour un médecin"),
    // On n'envoie pas de nouveau lien à un compte qui fonctionne déjà.
    COMPTE_DEJA_ACTIF(HttpStatus.CONFLICT, "Ce compte est déjà activé"),
    // 400 et pas 401 : un 401 ferait croire au front que la session a expiré.
    MOT_DE_PASSE_INCORRECT(HttpStatus.BAD_REQUEST, "L'ancien mot de passe est incorrect"),
    // Le numéro d'ordre identifie un médecin auprès de l'Ordre : deux médecins ne peuvent pas avoir le même.
    NUMERO_ORDRE_DEJA_UTILISE(HttpStatus.CONFLICT, "Le numéro d'ordre est déjà utilisé")
    ;

    private final HttpStatus statut;
    private final String titre;
}
