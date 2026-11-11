package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.enums.TypeRapport;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RapportDto {

    private TypeRapport type;

    private LocalDate periodeDebut;

    private LocalDate periodeFin;

    private LocalDateTime dateGeneration;

    private String resume;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer administrateurId;
}
