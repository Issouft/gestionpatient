package com.forceN.gestionpatient.dto;

import com.forceN.gestionpatient.entities.Patient;
import com.forceN.gestionpatient.entities.Secretaire;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DossierPatientDto {

    private String antecedents;

    private Patient patient;

    private Secretaire creePar;

    private List<com.forceN.gestionpatient.entities.Consultation> consultations = new ArrayList<>();

    /*
    public void ajouterConsultation(Consultation consultation) {
        consultations.add(consultation);
        consultation.setDossierPatient(this);
    }

     */
}
