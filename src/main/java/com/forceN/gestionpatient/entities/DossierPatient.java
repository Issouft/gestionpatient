package com.forceN.gestionpatient.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DossierPatient extends AbstractEntity {

    @Column(name = "Antecedents")
    private String antecedents;

}
