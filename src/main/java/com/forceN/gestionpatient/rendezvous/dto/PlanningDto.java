package com.forceN.gestionpatient.rendezvous.dto;

import java.util.ArrayList;
import java.util.List;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlanningDto {

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer medecinId;

    // ajouterCreneau / supprimerCreneau retirés : ils modifiaient des entités, ce travail revient au service.
    private List<CreneauDto> creneaux = new ArrayList<>();
}
