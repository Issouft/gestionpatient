package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.Medicament;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.Pharmacien;
import com.forceN.gestionpatient.medical.enums.StatutDemande;
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
