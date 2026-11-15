package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.Rapport;
import com.forceN.gestionpatient.medical.enums.TypeRapport;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Rapport.
 * Requête (POST /rapports) : type, periodeDebut, periodeFin (les deux jours sont inclus).
 * Le résumé est calculé par l'application.
 */
@Getter
@Setter
@NoArgsConstructor
public class RapportDto {

    private Integer id;

    @NotNull(message = "Le type de rapport est obligatoire")
    private TypeRapport type;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate periodeDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate periodeFin;

    private Instant dateGeneration;

    private String resume;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer administrateurId;

    /** Entité → DTO. Les dates de période, stockées en Instant, redeviennent des jours dans le fuseau donné. */
    public static RapportDto fromEntity(Rapport rapport, ZoneId fuseau) {
        RapportDto dto = new RapportDto();
        dto.setId(rapport.getId());
        dto.setType(rapport.getType());
        dto.setPeriodeDebut(LocalDate.ofInstant(rapport.getPeriodeDebut(), fuseau));
        dto.setPeriodeFin(LocalDate.ofInstant(rapport.getPeriodeFin(), fuseau));
        dto.setDateGeneration(rapport.getDateGeneration());
        dto.setResume(rapport.getResume());
        dto.setAdministrateurId(rapport.getAdministrateur().getId());
        return dto;
    }
}
