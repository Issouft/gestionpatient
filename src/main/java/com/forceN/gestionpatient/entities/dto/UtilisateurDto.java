package com.forceN.gestionpatient.entities.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UtilisateurDto {

    private String nom;

    private String prenom;

    private String email;

    private String password;

    private boolean actif = true;
}
