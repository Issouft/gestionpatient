package com.forceN.gestionpatient.rendezvous.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class CreneauDto {

    private LocalDate date;

    private LocalTime heureDebut;

    private LocalTime heureFin;

    private boolean disponible = true;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer planningId;
}
