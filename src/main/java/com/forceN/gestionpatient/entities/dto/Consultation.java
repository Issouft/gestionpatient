package com.forceN.gestionpatient.entities.dto;

import com.forceN.gestionpatient.entities.DossierPatient;
import com.forceN.gestionpatient.entities.Medecin;
import com.forceN.gestionpatient.entities.RendezVous;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Consultation {
    private LocalDateTime date;

    private String observations;
    private String diagnostic;

    private DossierPatient dossierPatient;

    private Medecin medecin;

    private RendezVous rendezVous;
}
