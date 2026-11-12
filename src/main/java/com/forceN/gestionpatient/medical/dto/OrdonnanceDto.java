package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.Ordonnance;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrdonnanceDto {

    private Integer id;

    private Instant dateEmission;

    // Uniquement l'id pour éviter la boucle infinie avec ConsultationDto
    private Integer consultationId;

    @Builder.Default
    private List<LigneOrdonnanceDto> lignes = new ArrayList<>();

    public static OrdonnanceDto fromEntity(Ordonnance ordonnance) {
        if (ordonnance == null) {
            return null;
        }
        return OrdonnanceDto.builder()
                .id(ordonnance.getId())
                .dateEmission(ordonnance.getDateEmission())
                .consultationId(ordonnance.getConsultation() == null ? null : ordonnance.getConsultation().getId())
                .lignes(
                        ordonnance.getLignes() == null
                                ? new ArrayList<>()
                                : ordonnance.getLignes().stream()
                                .map(LigneOrdonnanceDto::fromEntity)
                                .toList()
                )
                .build();
    }

    public static Ordonnance toEntity(OrdonnanceDto dto) {
        if (dto == null) {
            return null;
        }

        Ordonnance ordonnance = new Ordonnance();
        ordonnance.setId(dto.getId());
        ordonnance.setDateEmission(dto.getDateEmission());

        if (dto.getLignes() != null) {
            dto.getLignes().forEach(ligneDto -> {
                LigneOrdonnanceDto ligne = LigneOrdonnanceDto.toEntity(ligneDto);
                ligne.setOrdonnanceId(ordonnance); // renseigne la clé étrangère
                ordonnance.getLignes().add(ligne);
            });
        }

        // La consultation n'est pas reconstruite ici : elle est affectée dans le
        // service via ordonnance.setConsultation(...) à partir de consultationId.

        return ordonnance;
    }
}