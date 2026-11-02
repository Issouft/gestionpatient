package com.forceN.gestionpatient.dto;

import com.forceN.gestionpatient.entities.Medicament;
import com.forceN.gestionpatient.entities.Patient;
import com.forceN.gestionpatient.entities.Pharmacien;
import com.forceN.gestionpatient.entities.enums.StatutDemande;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DemandeDisponibiliteDto {

    private LocalDateTime dateDemande;

    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    private String reponse;

    private Patient patient;

    private Medicament medicament;

    private Pharmacien pharmacien;

    /*
    public void valider(String reponse) {
        this.statut = StatutDemande.VALIDEE;
        this.reponse = reponse;
    }

    public void refuser(String reponse) {
        this.statut = StatutDemande.REFUSEE;
        this.reponse = reponse;
    }

     */
}
