package com.forceN.gestionpatient.rendezvous.dto;

import com.forceN.gestionpatient.rendezvous.entities.Creneau;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.Secretaire;
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

    private Patient patient;

    private Medecin medecin;
    private Secretaire secretaire;

    private Creneau creneau;
}
