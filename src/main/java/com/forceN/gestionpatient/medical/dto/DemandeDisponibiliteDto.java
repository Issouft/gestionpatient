package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.DemandeDisponibilite;
import com.forceN.gestionpatient.medical.enums.StatutDemande;
import com.forceN.gestionpatient.utilisateur.dto.PatientDto;
import com.forceN.gestionpatient.utilisateur.dto.PharmacienDto;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DemandeDisponibiliteDto {

    private Integer id;

    private Instant dateDemande;

    @Builder.Default
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    private String reponse;

    private PatientDto patient;

    private MedicamentDto medicament;

    private PharmacienDto pharmacien;

    public static DemandeDisponibiliteDto fromEntity(DemandeDisponibilite demande) {
        if (demande == null) {
            return null;
        }
        return DemandeDisponibiliteDto.builder()
                .id(demande.getId())
                .dateDemande(demande.getDateDemande())
                .statut(demande.getStatut())
                .reponse(demande.getReponse())
                .patient(PatientDto.fromEntity(demande.getPatient()))
                .medicament(MedicamentDto.fromEntity(demande.getMedicament()))
                .pharmacien(PharmacienDto.fromEntity(demande.getPharmacien()))
                .build();
    }

    public static DemandeDisponibilite toEntity(DemandeDisponibiliteDto dto) {
        if (dto == null) {
            return null;
        }

        DemandeDisponibilite demande = new DemandeDisponibilite();
        demande.setId(dto.getId());
        demande.setDateDemande(dto.getDateDemande());
        demande.setStatut(dto.getStatut());
        demande.setReponse(dto.getReponse());
        demande.setPatient(PatientDto.toEntity(dto.getPatient()));
        demande.setMedicament(MedicamentDto.toEntity(dto.getMedicament()));
        demande.setPharmacien(PharmacienDto.toEntity(dto.getPharmacien()));

        return demande;
    }
}