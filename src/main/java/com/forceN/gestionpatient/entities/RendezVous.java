package com.forceN.gestionpatient.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.Date;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RendezVous extends AbstractEntity{

    @Column(name = "motif_rendez_vous")
    private String motif;

    @Column(name = "statut_rendez_vous")
    private String statut;

    @Column(name = "date_rendezVous")
    private Date dateRendezVous;
}
