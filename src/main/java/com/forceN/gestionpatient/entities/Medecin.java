package com.forceN.gestionpatient.entities;

import com.forceN.gestionpatient.entities.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "medecin")
public class Medecin extends Utilisateur {

    private String specialite;

    @Column(name = "numero_ordre", unique = true)
    private String numeroOrdre;

    @PrePersist
    void definirRole() {
        setRole(Role.MEDECIN);
    }
}
