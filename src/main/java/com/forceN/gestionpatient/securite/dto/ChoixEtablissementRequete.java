package com.forceN.gestionpatient.securite.dto;

import jakarta.validation.constraints.NotNull;

/** Corps de POST /auth/etablissement : l'établissement où le personnel veut travailler. */
public record ChoixEtablissementRequete(
        @NotNull(message = "L'établissement est obligatoire")
        Integer etablissementId) {
}
