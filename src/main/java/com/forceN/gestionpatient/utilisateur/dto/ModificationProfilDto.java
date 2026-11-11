package com.forceN.gestionpatient.utilisateur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Données envoyées pour modifier son profil (PUT /utilisateurs/moi).
 * Ni email (c'est l'identifiant de connexion), ni rôle, ni mot de passe (route à part),
 * ni spécialité ou numéro d'ordre du médecin (gérés par l'administrateur).
 */
@Getter
@Setter
@NoArgsConstructor
public class ModificationProfilDto {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    // Patient uniquement. Facultatifs : absents (null), ils restent inchangés.
    // @Pattern accepte null mais refuse "" et "   " (il faut au moins un caractère qui n'est pas un espace).
    @Pattern(regexp = ".*\\S.*", message = "Le téléphone ne peut pas être vide")
    private String telephone;

    @Pattern(regexp = ".*\\S.*", message = "L'adresse ne peut pas être vide")
    private String adresse;
}
