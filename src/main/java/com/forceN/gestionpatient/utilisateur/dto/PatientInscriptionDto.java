package com.forceN.gestionpatient.utilisateur.dto;

import java.time.LocalDate;

import com.forceN.gestionpatient.utilisateur.enums.Sexe;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Email(message = "Le format de l'email est invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères")
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    // Champs propres aux patients
    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @NotNull(message = "Le sexe est obligatoire")
    private Sexe sexe;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

}
