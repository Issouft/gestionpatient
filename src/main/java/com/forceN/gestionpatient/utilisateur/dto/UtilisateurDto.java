package com.forceN.gestionpatient.utilisateur.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.utilisateur.enums.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Champs communs à tous les utilisateurs, utilisés dans les réponses. Jamais de mot de passe ici. */
@Getter
@Setter
@NoArgsConstructor
public class UtilisateurDto {

    private Integer id;

    private String nom;

    private String prenom;

    private String email;

    private Role role;

    // Le compte peut-il se connecter ? (Sur toute la plateforme.)
    private boolean actif;

    // Rempli seulement dans la liste du personnel (GET /personnel) : ACTIVE, ou SUSPENDUE dans l'établissement
    // de l'administrateur. Absent des autres réponses (NON_NULL).
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private StatutAffiliation statutDansEtablissement;
}
