package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.Ordonnance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Ordonnance.
 * Requête (POST /consultations/{id}/ordonnance) : lignes seulement. La consultation vient de l'URL.
 */
@Getter
@Setter
@NoArgsConstructor
public class OrdonnanceDto {

    private Integer id;

    private Instant dateEmission;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer consultationId;

    // @Valid : chaque ligne est vérifiée elle aussi.
    @NotEmpty(message = "L'ordonnance doit contenir au moins un médicament")
    @Valid
    private List<LigneOrdonnanceDto> lignes = new ArrayList<>();

    /** Entité → DTO, avec ses lignes. */
    public static OrdonnanceDto fromEntity(Ordonnance ordonnance) {
        OrdonnanceDto dto = new OrdonnanceDto();
        dto.setId(ordonnance.getId());
        dto.setDateEmission(ordonnance.getDateEmission());
        dto.setConsultationId(ordonnance.getConsultation().getId());
        dto.setLignes(ordonnance.getLignes().stream().map(LigneOrdonnanceDto::fromEntity).toList());
        return dto;
    }
}
