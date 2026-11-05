package com.forceN.gestionpatient.utilisateur.dto;

import com.forceN.gestionpatient.utilisateur.enums.Role;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Données envoyées par l'administrateur pour créer un membre du personnel (POST /personnel).
 * Le rôle choisit le type de compte : MEDECIN, SECRETAIRE ou PHARMACIEN.
 */

@Getter
@Setter
@NoArgsConstructor
public class PersonnelCreationDto {
    // Champs communs à tous les utilisateurs (mêmes règles que l'inscription)
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Email(message = "Le format de l'email est invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    // Choisi par l'admin. PATIENT et ADMINISTRATEUR seront refusés par le service.
    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    // Propres au médecin : facultatifs ici, le service les exige si le rôle est MEDECIN.
    private String specialite;
    private String numeroOrdre;
}
