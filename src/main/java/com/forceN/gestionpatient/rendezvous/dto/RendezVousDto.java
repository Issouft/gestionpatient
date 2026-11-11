package com.forceN.gestionpatient.rendezvous.dto;

import com.forceN.gestionpatient.rendezvous.enums.StatutRendezVous;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RendezVousDto {

    private LocalDateTime dateHeure;

    private String motif;

    private StatutRendezVous statut = StatutRendezVous.EN_ATTENTE;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer patientId;

    private Integer medecinId;
    private Integer secretaireId;

    private Integer creneauId;
}
