package com.forceN.gestionpatient.entities;

import com.forceN.gestionpatient.entities.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "pharmacien")
public class Pharmacien extends Utilisateur {

    @PrePersist
    void definirRole() {
        setRole(Role.PHARMACIEN);
    }
}
