package com.forceN.gestionpatient.dto;

import com.forceN.gestionpatient.entities.enums.Role;
import com.forceN.gestionpatient.entities.enums.Sexe;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;

import java.time.LocalDate;
import java.time.Period;

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
