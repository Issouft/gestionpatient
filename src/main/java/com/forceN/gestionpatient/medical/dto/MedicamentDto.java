package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.Medicament;
import com.forceN.gestionpatient.utilisateur.dto.PharmacienDto;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicamentDto {

    private Integer id;

    private String nom;
    private String dosage;
    private int stock;
    private int seuilAlerte;

    private PharmacienDto pharmacien;

    public boolean estDisponible() {
        return stock > 0;
    }

    public boolean estSousSeuilAlerte() {
        return stock <= seuilAlerte;
    }

    public static MedicamentDto fromEntity(Medicament medicament) {
        if (medicament == null) {
            return null;
        }
        return MedicamentDto.builder()
                .id(medicament.getId())
                .nom(medicament.getNom())
                .dosage(medicament.getDosage())
                .stock(medicament.getStock())
                .seuilAlerte(medicament.getSeuilAlerte())
                .pharmacien(PharmacienDto.fromEntity(medicament.getPharmacien()))
                .build();
    }

    public static Medicament toEntity(MedicamentDto dto) {
        if (dto == null) {
            return null;
        }

        Medicament medicament = new Medicament();
        medicament.setId(dto.getId());
        medicament.setNom(dto.getNom());
        medicament.setDosage(dto.getDosage());
        medicament.setStock(dto.getStock());
        medicament.setSeuilAlerte(dto.getSeuilAlerte());
        medicament.setPharmacien(PharmacienDto.toEntity(dto.getPharmacien()));

        return medicament;
    }
}