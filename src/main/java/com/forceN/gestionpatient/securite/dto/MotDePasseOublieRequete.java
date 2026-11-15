package com.forceN.gestionpatient.securite.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Email envoyé pour recevoir un lien de réinitialisation (POST /auth/mot-de-passe-oublie). */
@Getter
@Setter
@NoArgsConstructor
public class MotDePasseOublieRequete {

    @Email(message = "Le format de l'email est invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;
}
