package com.forceN.gestionpatient.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor

@Entity
public class Planning extends AbstractEntity {

    @ManyToOne
    private Medecin medecin;

    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Integer dureeConsultation;

    @Enumerated(EnumType.STRING)
    private EnumType type;

    private String commentaire;
}
