package com.forceN.gestionpatient.utilisateur.dto;

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

    private boolean actif;
}
