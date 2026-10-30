package com.forceN.gestionpatient.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LigneOrdonnance extends AbstractEntity {

    private String medicament;
    private String posologie;
    private int quantite;
    private String dureeTraitement;

    @ManyToOne
    private Ordonnance ordonnance;
}