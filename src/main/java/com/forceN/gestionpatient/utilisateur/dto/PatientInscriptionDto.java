package com.forceN.gestionpatient.utilisateur.dto;

import java.time.LocalDate;

import com.forceN.gestionpatient.utilisateur.enums.Sexe;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
public class PatientInscriptionDto {
    // Champs communs à tous les utilisateurs
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;

    // Champ propre aux patients
    private LocalDate dateNaissance;
    private Sexe sexe;
    private String telephone;
    private String adresse;

}
