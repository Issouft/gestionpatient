package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.Secretaire;
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

    private List<com.forceN.gestionpatient.medical.entities.Consultation> consultations = new ArrayList<>();

    /*
    public void ajouterConsultation(ConsultationDto consultation) {
        consultations.add(consultation);
        consultation.setDossierPatient(this);
    }

     */
}
