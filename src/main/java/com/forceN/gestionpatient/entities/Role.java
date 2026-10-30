package com.forceN.gestionpatient.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Role extends AbstractEntity{
    @Column(name = "nom_Role")
    private String nomRole;

    @Column(name = "Libelle")
    private String libelle;
    @Column(name = "Description")
    private String description;
}
