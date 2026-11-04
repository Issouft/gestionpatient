package com.forceN.gestionpatient.utilisateur.dto;

import java.time.LocalDate;

import com.forceN.gestionpatient.utilisateur.enums.Sexe;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Données envoyées par le client pour s'inscrire (POST /auth/inscription).
 * Ni id, ni rôle, ni actif : c'est le serveur qui les fixe.
 */
@Getter
@Setter
@NoArgsConstructor
public class PatientInscriptionDto {
    // Champs communs à tous les utilisateurs
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;

    // Champs propres aux patients
    private LocalDate dateNaissance;
    private Sexe sexe;
    private String telephone;
    private String adresse;

}
