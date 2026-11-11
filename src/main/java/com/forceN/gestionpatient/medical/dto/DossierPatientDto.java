package com.forceN.gestionpatient.medical.dto;

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

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer patientId;

    private Integer creeParId;

    private List<ConsultationDto> consultations = new ArrayList<>();

    /*
    public void ajouterConsultation(ConsultationDto consultation) {
        consultations.add(consultation);
        consultation.setDossierPatient(this);
    }

     */
}
