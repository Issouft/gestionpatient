package com.forceN.gestionpatient.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Medicament extends AbstractEntity {

    private String nom;
    private String dosage;
    private int stock;

    @Column(name = "seuil_alerte")
    private int seuilAlerte;

    // Pharmacien 1 — * Medicament ("gère le stock")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacien_id")
    @ToString.Exclude
    private Pharmacien pharmacien;

    public boolean estDisponible() {
        return stock > 0;
    }

    public boolean estSousSeuilAlerte() {
        return stock <= seuilAlerte;
    }
}
