package com.forceN.gestionpatient.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Creneau extends AbstractEntity {

    private LocalDate date;

    @Column(name = "heure_debut")
    private LocalTime heureDebut;

    @Column(name = "heure_fin")
    private LocalTime heureFin;

    @Builder.Default
    private boolean disponible = true;

    // Planning 1 — * Creneau (composition : pas de créneau sans planning)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_id", nullable = false)
    @ToString.Exclude
    private Planning planning;
}
