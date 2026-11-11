package com.forceN.gestionpatient.medical.dto;

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

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer ordonnanceId;

    private Integer medicamentId;
}
