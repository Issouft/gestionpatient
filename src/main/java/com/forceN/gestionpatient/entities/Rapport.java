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
public class Rapport extends AbstractEntity{

    @Column(name = "type_Rapport")
    private String typeRapport;

    @Column(name = "date_generation")
    private Date dateGeneration;

    @Column(name = "resume_rapport")
    private String resume;
}
