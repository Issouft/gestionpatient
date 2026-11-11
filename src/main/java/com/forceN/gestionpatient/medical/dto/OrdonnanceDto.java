package com.forceN.gestionpatient.medical.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrdonnanceDto {


    private LocalDate dateEmission;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer consultationId;

    private List<LigneOrdonnanceDto> lignes = new ArrayList<>();

    /*public void ajouterLigne(LigneOrdonnance ligne) {
        lignes.add(ligne);
        ligne.setOrdonnance(this);
    }
     */
}
