package com.forceN.gestionpatient.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Disponibilite extends AbstractEntity {
    @Column(name = "Reponse")
    private String reponse;
    @Column(name = "Disponibilite")
    private String statutDemande;
}
