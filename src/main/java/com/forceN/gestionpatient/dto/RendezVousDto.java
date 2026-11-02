package com.forceN.gestionpatient.dto;

import com.forceN.gestionpatient.entities.Creneau;
import com.forceN.gestionpatient.entities.Medecin;
import com.forceN.gestionpatient.entities.Patient;
import com.forceN.gestionpatient.entities.Secretaire;
import com.forceN.gestionpatient.entities.enums.StatutRendezVous;
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

    private Patient patient;

    private Medecin medecin;
    private Secretaire secretaire;

    private Creneau creneau;
}
