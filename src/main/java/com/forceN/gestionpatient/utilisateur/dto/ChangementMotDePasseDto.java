package com.forceN.gestionpatient.utilisateur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Données envoyées pour changer son mot de passe (PUT /utilisateurs/moi/mot-de-passe) :
 * l'ancien, pour prouver que c'est bien la personne, et le nouveau.
 */
@Getter
@Setter
@NoArgsConstructor
public class ChangementMotDePasseDto {

    // Pas de @Size ici : on ne fait que le comparer à celui enregistré.
    @NotBlank(message = "L'ancien mot de passe est obligatoire")
    private String ancienMotDePasse;

    // Mêmes règles que l'inscription et l'activation.
    @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères")
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    private String nouveauMotDePasse;
}
