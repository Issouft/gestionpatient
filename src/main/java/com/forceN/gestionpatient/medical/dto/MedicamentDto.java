package com.forceN.gestionpatient.medical.dto;

import com.forceN.gestionpatient.medical.entities.Medicament;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Médicament.
 * Requête (POST /medicaments) : nom, dosage, stock, seuilAlerte.
 * Le stock n'est donné qu'à la création : ensuite, il baisse seul à chaque retrait confirmé.
 */
@Getter
@Setter
@NoArgsConstructor
public class MedicamentDto {

    private Integer id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255, message = "Le nom ne doit pas dépasser 255 caractères")
    private String nom;

    // Exemple : "500 mg".
    @NotBlank(message = "Le dosage est obligatoire")
    @Size(max = 255, message = "Le dosage ne doit pas dépasser 255 caractères")
    private String dosage;

    @PositiveOrZero(message = "Le stock ne peut pas être négatif")
    private int stock;

    @PositiveOrZero(message = "Le seuil d'alerte ne peut pas être négatif")
    private int seuilAlerte;

    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer pharmacienId;

    // "is" : Jackson les envoie dans la réponse sous les noms "disponible" et "sousSeuilAlerte".
    public boolean isDisponible() {
        return stock > 0;
    }

    public boolean isSousSeuilAlerte() {
        return stock <= seuilAlerte;
    }

    /** Entité → DTO. */
    public static MedicamentDto fromEntity(Medicament medicament) {
        MedicamentDto dto = new MedicamentDto();
        dto.setId(medicament.getId());
        dto.setNom(medicament.getNom());
        dto.setDosage(medicament.getDosage());
        dto.setStock(medicament.getStock());
        dto.setSeuilAlerte(medicament.getSeuilAlerte());
        dto.setPharmacienId(medicament.getPharmacien() == null ? null : medicament.getPharmacien().getId());
        return dto;
    }

    /** DTO → entité. Le service ajoute le pharmacien. */
    public Medicament toEntity() {
        return Medicament.builder()
                .nom(nom.trim())
                .dosage(dosage.trim())
                .stock(stock)
                .seuilAlerte(seuilAlerte)
                .build();
    }
}
