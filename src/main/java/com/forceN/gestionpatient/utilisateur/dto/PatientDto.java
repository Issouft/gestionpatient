package com.forceN.gestionpatient.utilisateur.dto;

import java.time.LocalDate;
import java.time.Period;

import com.forceN.gestionpatient.utilisateur.enums.Sexe;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Patient renvoyé au client. Les champs communs (id, nom, email...) viennent de UtilisateurDto. */
@Getter
@Setter
@NoArgsConstructor
public class PatientDto extends UtilisateurDto {

    private LocalDate dateNaissance;

    private Sexe sexe;

    private String telephone;

    private String adresse;

    /** L'âge n'est pas stocké : il est calculé à partir de la date de naissance. */
    public Integer getAge() {
        return dateNaissance == null ? null : Period.between(dateNaissance, LocalDate.now()).getYears();
    }
}
