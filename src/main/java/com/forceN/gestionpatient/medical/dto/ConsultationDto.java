package com.forceN.gestionpatient.medical.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ConsultationDto {
    private LocalDateTime date;

    private String observations;
    private String diagnostic;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer dossierPatientId;

    private Integer medecinId;

    private Integer rendezVousId;
}
