package com.forceN.gestionpatient.entities;

import com.forceN.gestionpatient.entities.enums.Role;
import com.forceN.gestionpatient.entities.enums.Sexe;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.Period;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "patient")
public class Patient extends Utilisateur {

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Enumerated(EnumType.STRING)
    private Sexe sexe;

    private String telephone;
    private String adresse;

    /** Remplace l'ancien champ "age" : calculé à partir de la date de naissance. */
    public Integer getAge() {
        return dateNaissance == null ? null : Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    @PrePersist
    void definirRole() {
        setRole(Role.PATIENT);
    }
}
