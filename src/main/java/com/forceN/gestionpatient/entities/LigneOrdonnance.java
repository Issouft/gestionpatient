package com.forceN.gestionpatient.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** Classe d'association entre Ordonnance et Medicament. */
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ligne_ordonnance")
public class LigneOrdonnance extends AbstractEntity {

    private int quantite;
    private String posologie;

    @Column(name = "duree_jours")
    private int dureeJours;

    // Ordonnance 1 — 1..* LigneOrdonnance (composition)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ordonnance_id", nullable = false)
    @ToString.Exclude
    private Ordonnance ordonnance;

    // LigneOrdonnance * — 1 Medicament ("prescrit")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medicament_id", nullable = false)
    @ToString.Exclude
    private Medicament medicament;
}
