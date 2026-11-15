package com.forceN.gestionpatient.securite.dto;

import com.forceN.gestionpatient.etablissement.dto.AccesEtablissementDto;

import java.util.List;

/**
 * Réponse de la connexion et du choix d'établissement.
 *
 * @param jeton           à envoyer ensuite dans l'en-tête Authorization: Bearer ...
 * @param etablissementId l'établissement où ce jeton permet de travailler ; null pour un patient,
 *                        ou quand le personnel doit encore choisir (plusieurs établissements, ou aucun)
 * @param etablissements  les établissements où la personne travaille, avec ses rôles (vide pour un patient)
 */
public record ConnexionReponse(String jeton, Integer etablissementId, List<AccesEtablissementDto> etablissements) {
}
