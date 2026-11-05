package com.forceN.gestionpatient.utilisateur.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MedecinDto {
    private String specialite;

    private String numeroOrdre;

   /* @PrePersist
    void definirRole() {
        setRole(Role.MEDECIN);
    }

    */
}
