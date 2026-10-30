package com.forceN.gestionpatient.entities;


import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Clinique extends AbstractEntity{
    private String nom;
    private String adresse;
    private String telephone;

}
