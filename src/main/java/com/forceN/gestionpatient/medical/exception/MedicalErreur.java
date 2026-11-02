package com.forceN.gestionpatient.medical.exception;

import com.forceN.gestionpatient.commun.exception.CodeErreur;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MedicalErreur implements CodeErreur {
    // Ajouter les erreurs ici, avant le point-virgule. Exemple : NOM_ERREUR(HttpStatus.NOT_FOUND, "Titre de l'erreur");
    ;

    private final HttpStatus statut;
    private final String titre;
}
