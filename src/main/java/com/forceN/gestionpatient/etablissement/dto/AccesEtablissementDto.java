package com.forceN.gestionpatient.etablissement.dto;

import com.forceN.gestionpatient.utilisateur.enums.Role;

import java.util.Set;

/**
 * Un établissement où la personne connectée peut travailler, avec ses rôles dans cet établissement.
 * Renvoyé à la connexion, pour qu'elle choisisse son établissement.
 */
public record AccesEtablissementDto(Integer etablissementId, String etablissementNom, Set<Role> roles) {
}
