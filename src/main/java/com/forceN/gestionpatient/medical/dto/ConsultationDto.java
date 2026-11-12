package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.Consultation;
import com.forceN.gestionpatient.rendezvous.dto.RendezVousDto;
import com.forceN.gestionpatient.utilisateur.dto.MedecinDto;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsultationDto {

    private Integer id;
    private Instant date;

    private String observations;
    private String diagnostic;

    private DossierPatientDto dossierPatient;

    private MedecinDto medecin;

    private RendezVousDto rendezVous;

    public static ConsultationDto fromEntity(Consultation consultation) {
        if (consultation == null) {
            return null;
        }
        return ConsultationDto.builder()
                .id(consultation.getId())
                .date(consultation.getDate())
                .observations(consultation.getObservations())
                .diagnostic(consultation.getDiagnostic())
                .dossierPatient(DossierPatientDto.fromEntity(consultation.getDossierPatient()))
                .medecin(MedecinDto.fromEntity(consultation.getMedecin()))
                .rendezVous(RendezVousDto.fromEntity(consultation.getRendezVous()))
                .build();
    }

    public static Consultation toEntity(ConsultationDto dto) {
        if (dto == null) {
            return null;
        }

        Consultation consultation = new Consultation();
        consultation.setId(dto.getId());
        consultation.setDate(dto.getDate());
        consultation.setDiagnostic(dto.getDiagnostic());
        consultation.setObservations(dto.getObservations());
        consultation.setMedecin(MedecinDto.toEntity(dto.getMedecin()));
        consultation.setDossierPatient(DossierPatientDto.toEntity(dto.getDossierPatient()));
        consultation.setRendezVous(RendezVousDto.toEntity(dto.getRendezVous()));

        return consultation;
    }
}