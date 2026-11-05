package com.forceN.gestionpatient.utilisateur.dto;

import java.time.LocalDate;
import java.time.Period;

import com.forceN.gestionpatient.utilisateur.enums.Sexe;

public class PatientDto {

    private LocalDate dateNaissance;

    private Sexe sexe;

    private String telephone;
    private String adresse;
    public Integer getAge() {
        return dateNaissance == null ? null : Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    /*@PrePersist
    void definirRole() {
        setRole(Role.PATIENT);
    }

     */
}
