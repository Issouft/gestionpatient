package com.forceN.gestionpatient.utilisateur.dto;

import com.forceN.gestionpatient.utilisateur.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
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
