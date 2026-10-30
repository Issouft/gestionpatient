package com.forceN.gestionpatient.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Secretaire extends AbstractEntity{
    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "contact")
    private String contact;

    @Column(name = "departement")
    private String departement;
}
