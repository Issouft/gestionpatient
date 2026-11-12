package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.LigneOrdonnance;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LigneOrdonnanceDto {

    private Integer id;

    private int quantite;
    private String posologie;
    private int dureeJours;

    // Uniquement l'id pour éviter la boucle infinie avec OrdonnanceDto
    private Integer ordonnanceId;

    private MedicamentDto medicament;

    public static LigneOrdonnanceDto fromEntity(LigneOrdonnance ligne) {
        if (ligne == null) {
            return null;
        }
        return LigneOrdonnanceDto.builder()
                .id(ligne.getId())
                .quantite(ligne.getQuantite())
                .posologie(ligne.getPosologie())
                .dureeJours(ligne.getDureeJours())
                .ordonnanceId(ligne.getOrdonnance() == null ? null : ligne.getOrdonnance().getId())
                .medicament(MedicamentDto.fromEntity(ligne.getMedicament()))
                .build();
    }

    public static LigneOrdonnance toEntity(LigneOrdonnanceDto dto) {
        if (dto == null) {
            return null;
        }

        LigneOrdonnance ligneOrdonnance = new LigneOrdonnance();
        ligneOrdonnance.setId(dto.getId());
        ligneOrdonnance.setQuantite(dto.getQuantite());
        ligneOrdonnance.setPosologie(dto.getPosologie());
        ligneOrdonnance.setDureeJours(dto.getDureeJours());
        ligneOrdonnance.setMedicament(MedicamentDto.toEntity(dto.getMedicament()));

        // L'ordonnance n'est pas reconstruite ici : elle est affectée côté
        // OrdonnanceDto.toEntity (ou dans le service) via ligne.setOrdonnance(...).

        return ligneOrdonnance;
    }
}