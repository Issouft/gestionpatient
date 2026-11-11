package com.forceN.gestionpatient.medical.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MedicamentDto {
    private String nom;
    private String dosage;
    private int stock;
    private int seuilAlerte;
    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer pharmacienId;

    public boolean estDisponible() {
        return stock > 0;
    }

    public boolean estSousSeuilAlerte() {
        return stock <= seuilAlerte;
    }
}
