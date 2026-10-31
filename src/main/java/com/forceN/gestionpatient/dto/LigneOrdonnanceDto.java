package com.forceN.gestionpatient.dto;

import com.forceN.gestionpatient.entities.Medicament;
import com.forceN.gestionpatient.entities.Ordonnance;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LigneOrdonnanceDto {
    private int quantite;
    private String posologie;

    private int dureeJours;

    private Ordonnance ordonnance;

    private Medicament medicament;
}
