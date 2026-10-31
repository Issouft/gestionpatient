package com.forceN.gestionpatient.entities.dto;

import com.forceN.gestionpatient.entities.Administrateur;
import com.forceN.gestionpatient.entities.enums.TypeRapport;
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

    private Administrateur administrateur;
}
