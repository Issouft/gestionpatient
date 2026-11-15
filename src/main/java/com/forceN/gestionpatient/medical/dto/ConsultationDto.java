package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.Consultation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Consultation.
 * Requête (POST /consultations) : rendezVousId, observations, diagnostic.
 * Le patient et le dossier se déduisent du rendez-vous ; le médecin, du jeton.
 */
@Getter
@Setter
@NoArgsConstructor
public class ConsultationDto {

    private Integer id;

    private Instant date;

    // 255 : la taille de la colonne en base.
    @NotBlank(message = "Les observations sont obligatoires")
    @Size(max = 255, message = "Les observations ne doivent pas dépasser 255 caractères")
    private String observations;

    @NotBlank(message = "Le diagnostic est obligatoire")
    @Size(max = 255, message = "Le diagnostic ne doit pas dépasser 255 caractères")
    private String diagnostic;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer dossierPatientId;

    private Integer medecinId;

    @NotNull(message = "Le rendez-vous est obligatoire")
    private Integer rendezVousId;

    /** Entité → DTO. getId() sur une relation LAZY ne déclenche pas de requête. */
    public static ConsultationDto fromEntity(Consultation consultation) {
        ConsultationDto dto = new ConsultationDto();
        dto.setId(consultation.getId());
        dto.setDate(consultation.getDate());
        dto.setObservations(consultation.getObservations());
        dto.setDiagnostic(consultation.getDiagnostic());
        dto.setDossierPatientId(consultation.getDossierPatient().getId());
        dto.setMedecinId(consultation.getMedecin().getId());
        dto.setRendezVousId(consultation.getRendezVous().getId());
        return dto;
    }

    /** DTO → entité. Le service ajoute la date, le médecin, le rendez-vous et le dossier. */
    public Consultation toEntity() {
        return Consultation.builder()
                .observations(observations.trim())
                .diagnostic(diagnostic.trim())
                .build();
    }
}
