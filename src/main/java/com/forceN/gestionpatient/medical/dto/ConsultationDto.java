package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.DossierPatient;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import com.forceN.gestionpatient.rendezvous.entities.RendezVous;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ConsultationDto {
    private LocalDateTime date;

    private String observations;
    private String diagnostic;

    private DossierPatient dossierPatient;

    private Medecin medecin;

    private RendezVous rendezVous;
}
