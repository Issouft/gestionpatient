package com.forceN.gestionpatient.medical.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.forceN.gestionpatient.medical.entities.Consultation;
import com.forceN.gestionpatient.medical.entities.LigneOrdonnance;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrdonnanceDto {


    private LocalDate dateEmission;

    private Consultation consultation;

    private List<LigneOrdonnance> lignes = new ArrayList<>();

    /*public void ajouterLigne(LigneOrdonnance ligne) {
        lignes.add(ligne);
        ligne.setOrdonnance(this);
    }
     */
}
