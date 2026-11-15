package com.forceN.gestionpatient.etablissement.dto;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.enums.TypeEtablissement;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;

/**
 * Un établissement de la plateforme.
 * Requête (POST /etablissements) : nom, type, et facultativement adresse, ville, telephone, email.
 * Réponse : en plus, id et actif.
 */
@Getter
@Setter
@NoArgsConstructor
public class EtablissementDto {

    private Integer id;

    // 255 : la taille des colonnes en base.
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255, message = "Le nom ne doit pas dépasser 255 caractères")
    private String nom;

    @NotNull(message = "Le type est obligatoire : CLINIQUE, CABINET, PHARMACIE ou LABORATOIRE")
    private TypeEtablissement type;

    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
    private String adresse;

    @Size(max = 255, message = "La ville ne doit pas dépasser 255 caractères")
    private String ville;

    @Size(max = 255, message = "Le téléphone ne doit pas dépasser 255 caractères")
    private String telephone;

    // Email de contact de l'établissement (pas un compte de connexion).
    @Email(message = "L'email n'est pas valide")
    @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères")
    private String email;

    // Réponse seulement : un nouvel établissement est actif. Ignoré dans une requête.
    private boolean actif;

    /** Entité → DTO. */
    public static EtablissementDto fromEntity(Etablissement etablissement) {
        EtablissementDto dto = new EtablissementDto();
        dto.setId(etablissement.getId());
        dto.setNom(etablissement.getNom());
        dto.setType(etablissement.getType());
        dto.setAdresse(etablissement.getAdresse());
        dto.setVille(etablissement.getVille());
        dto.setTelephone(etablissement.getTelephone());
        dto.setEmail(etablissement.getEmail());
        dto.setActif(etablissement.isActif());
        return dto;
    }

    /** DTO → entité, textes nettoyés (espaces autour retirés, vides → null). L'établissement est créé actif. */
    public Etablissement toEntity() {
        return Etablissement.builder()
                .nom(nom.trim())
                .type(type)
                .adresse(nettoyer(adresse))
                .ville(nettoyer(ville))
                .telephone(nettoyer(telephone))
                .email(email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT))
                .build();
    }

    private static String nettoyer(String texte) {
        return texte == null || texte.isBlank() ? null : texte.trim();
    }
}
