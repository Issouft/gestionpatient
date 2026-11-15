package com.forceN.gestionpatient.utilisateur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données envoyées par le super-administrateur pour inviter l'administrateur d'un établissement
 * (POST /etablissements/{id}/administrateurs). Pas de mot de passe : la personne le choisit via le lien reçu.
 */
public record AdministrateurInvitationDto(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 255, message = "Le nom ne doit pas dépasser 255 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 255, message = "Le prénom ne doit pas dépasser 255 caractères")
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Le format de l'email est invalide")
        @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères")
        String email) {
}
