package com.forceN.gestionpatient.securite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Données envoyées pour activer un compte (POST /auth/activation) :
 * le jeton reçu dans le lien de l'email et le mot de passe choisi.
 */
@Getter
@Setter
@NoArgsConstructor
public class ActivationRequete {

    @NotBlank(message = "Le jeton est obligatoire")
    private String jeton;

    // Mêmes règles que l'inscription d'un patient.
    @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères")
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
}
