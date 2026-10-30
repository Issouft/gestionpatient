package com.forceN.gestionpatient.entities;

import jakarta.persistence.Entity;
import lombok.*;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Medecin extends AbstractEntity{

    private String specialite;

    private String numeroOrdre;
}
