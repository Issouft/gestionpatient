package com.forceN.gestionpatient.dto;

import com.forceN.gestionpatient.entities.Planning;
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

    private Planning planning;
}
