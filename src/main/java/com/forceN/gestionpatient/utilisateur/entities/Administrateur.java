package com.forceN.gestionpatient.utilisateur.entities;

import com.forceN.gestionpatient.utilisateur.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "administrateur")
public class Administrateur extends Utilisateur {

    @PrePersist
    void definirRole() {
        setRole(Role.ADMINISTRATEUR);
    }
}
