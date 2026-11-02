package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.utilisateur.entities.Pharmacien;
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
    private Pharmacien pharmacien;

    public boolean estDisponible() {
        return stock > 0;
    }

    public boolean estSousSeuilAlerte() {
        return stock <= seuilAlerte;
    }
}
