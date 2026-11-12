package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.Consultation;
import com.forceN.gestionpatient.medical.entities.DossierPatient;
import com.forceN.gestionpatient.utilisateur.dto.PatientDto;
import com.forceN.gestionpatient.utilisateur.dto.SecretaireDto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DossierPatientDto {

    private Integer id;

    private String antecedents;

    private PatientDto patient;

    private SecretaireDto creePar;

    // Uniquement les ids pour éviter la boucle infinie avec ConsultationDto
    @Builder.Default
    private List<Integer> consultationIds = new ArrayList<>();

    public static DossierPatientDto fromEntity(DossierPatient dossierPatient) {
        if (dossierPatient == null) {
            return null;
        }
        return DossierPatientDto.builder()
                .id(dossierPatient.getId())
                .antecedents(dossierPatient.getAntecedents())
                .patient(PatientDto.fromEntity(dossierPatient.getPatient()))
                .creePar(SecretaireDto.fromEntity(dossierPatient.getCreePar()))
                .consultationIds(
                        dossierPatient.getConsultations() == null
                                ? new ArrayList<>()
                                : dossierPatient.getConsultations().stream()
                                .map(Consultation::getId)
                                .toList()
                )
                .build();
    }

    public static DossierPatient toEntity(DossierPatientDto dto) {
        if (dto == null) {
            return null;
        }

        DossierPatient dossierPatient = new DossierPatient();
        dossierPatient.setId(dto.getId());
        dossierPatient.setAntecedents(dto.getAntecedents());
        dossierPatient.setPatient(PatientDto.toEntity(dto.getPatient()));
        dossierPatient.setCreePar(SecretaireDto.toEntity(dto.getCreePar()));
        // Les consultations ne sont pas reconstruites ici : elles sont gérées
        // côté Consultation (qui porte la relation vers le dossier).

        return dossierPatient;
    }
}