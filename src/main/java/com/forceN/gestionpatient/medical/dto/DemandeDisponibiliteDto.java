package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.DemandeDisponibilite;
import com.forceN.gestionpatient.medical.enums.StatutDemande;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Demande de disponibilité d'un médicament.
 * Requête du patient (POST /demandes) : medicamentId et quantite.
 * Réponse du pharmacien (validation, refus) : reponse seulement, facultative. Ces routes n'utilisent pas @Valid :
 * le service vérifie la longueur de la réponse lui-même.
 */
@Getter
@Setter
@NoArgsConstructor
public class DemandeDisponibiliteDto {

    // 255 : la taille de la colonne en base. Vérifiée par le service.
    public static final int LONGUEUR_MAX_REPONSE = 255;

    private Integer id;

    private Instant dateDemande;

    @Positive(message = "La quantité doit être supérieure à 0")
    private int quantite;

    private StatutDemande statut;

    private String reponse;

    // Null tant que le pharmacien n'a pas confirmé que le patient a pris le produit.
    private Instant dateRetrait;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer patientId;

    @NotNull(message = "Le médicament est obligatoire")
    private Integer medicamentId;

    private String medicamentNom;

    private Integer pharmacienId;

    /** Entité → DTO. */
    public static DemandeDisponibiliteDto fromEntity(DemandeDisponibilite demande) {
        DemandeDisponibiliteDto dto = new DemandeDisponibiliteDto();
        dto.setId(demande.getId());
        dto.setDateDemande(demande.getDateDemande());
        dto.setQuantite(demande.getQuantite());
        dto.setStatut(demande.getStatut());
        dto.setReponse(demande.getReponse());
        dto.setDateRetrait(demande.getDateRetrait());
        dto.setPatientId(demande.getPatient().getId());
        dto.setMedicamentId(demande.getMedicament().getId());
        dto.setMedicamentNom(demande.getMedicament().getNom());
        dto.setPharmacienId(demande.getPharmacien() == null ? null : demande.getPharmacien().getId());
        return dto;
    }

    /** DTO → entité. Le service ajoute la date, le patient et le médicament. Le statut part de EN_ATTENTE. */
    public DemandeDisponibilite toEntity() {
        return DemandeDisponibilite.builder()
                .quantite(quantite)
                .build();
    }
}
