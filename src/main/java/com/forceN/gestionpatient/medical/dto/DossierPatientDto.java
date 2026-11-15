package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.DossierPatient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Dossier d'un patient.
 * Requête (POST /dossiers) : patientId et antecedents. Les autres champs sont remplis dans la réponse.
 */
@Getter
@Setter
@NoArgsConstructor
public class DossierPatientDto {

    private Integer id;

    // Facultatif. 255 : la taille de la colonne en base.
    @Size(max = 255, message = "Les antécédents ne doivent pas dépasser 255 caractères")
    private String antecedents;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    @NotNull(message = "Le patient est obligatoire")
    private Integer patientId;

    // Pour afficher le dossier sans une deuxième requête.
    private String patientNom;

    private String patientPrenom;

    private Integer creeParId;

    // Les plus récentes d'abord (voir @OrderBy dans DossierPatient).
    private List<ConsultationDto> consultations = new ArrayList<>();

    /** Entité → DTO, avec ses consultations. */
    public static DossierPatientDto fromEntity(DossierPatient dossier) {
        DossierPatientDto dto = new DossierPatientDto();
        dto.setId(dossier.getId());
        dto.setAntecedents(dossier.getAntecedents());
        dto.setPatientId(dossier.getPatient().getId());
        dto.setPatientNom(dossier.getPatient().getNom());
        dto.setPatientPrenom(dossier.getPatient().getPrenom());
        dto.setCreeParId(dossier.getCreePar() == null ? null : dossier.getCreePar().getId());
        dto.setConsultations(dossier.getConsultations().stream().map(ConsultationDto::fromEntity).toList());
        return dto;
    }

    /** DTO → entité. Le service ajoute le patient et la secrétaire. */
    public DossierPatient toEntity() {
        return DossierPatient.builder()
                .antecedents(antecedents == null || antecedents.isBlank() ? null : antecedents.trim())
                .build();
    }
}
