package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.LigneOrdonnance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Une ligne d'ordonnance : un médicament prescrit.
 * Requête : medicamentId, quantite, posologie, dureeJours.
 */
@Getter
@Setter
@NoArgsConstructor
public class LigneOrdonnanceDto {

    private Integer id;

    @Positive(message = "La quantité doit être supérieure à 0")
    private int quantite;

    @NotBlank(message = "La posologie est obligatoire")
    @Size(max = 255, message = "La posologie ne doit pas dépasser 255 caractères")
    private String posologie;

    @Positive(message = "La durée doit être supérieure à 0")
    private int dureeJours;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer ordonnanceId;

    @NotNull(message = "Le médicament est obligatoire")
    private Integer medicamentId;

    // Pour lire l'ordonnance sans chercher chaque médicament.
    private String medicamentNom;

    private String medicamentDosage;

    /** Entité → DTO. */
    public static LigneOrdonnanceDto fromEntity(LigneOrdonnance ligne) {
        LigneOrdonnanceDto dto = new LigneOrdonnanceDto();
        dto.setId(ligne.getId());
        dto.setQuantite(ligne.getQuantite());
        dto.setPosologie(ligne.getPosologie());
        dto.setDureeJours(ligne.getDureeJours());
        dto.setOrdonnanceId(ligne.getOrdonnance().getId());
        dto.setMedicamentId(ligne.getMedicament().getId());
        dto.setMedicamentNom(ligne.getMedicament().getNom());
        dto.setMedicamentDosage(ligne.getMedicament().getDosage());
        return dto;
    }

    /** DTO → entité. Le service ajoute le médicament ; l'ordonnance est reliée par Ordonnance.ajouterLigne(). */
    public LigneOrdonnance toEntity() {
        return LigneOrdonnance.builder()
                .quantite(quantite)
                .posologie(posologie.trim())
                .dureeJours(dureeJours)
                .build();
    }
}
