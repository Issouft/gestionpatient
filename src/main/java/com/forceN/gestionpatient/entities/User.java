package com.forceN.gestionpatient.entities;

import jakarta.persistence.Entity;

import lombok.*;

import java.util.Date;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User extends AbstractEntity{
    private String nom;

    private String prenom;

    private String email;

    private String password;

    private Date dateNaissance;

    private Date lastUpdateDate;

}
