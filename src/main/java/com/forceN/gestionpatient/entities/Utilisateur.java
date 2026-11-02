package com.forceN.gestionpatient.entities;

import com.forceN.gestionpatient.entities.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Classe abstraite du diagramme : Medecin, Secretaire, Patient, Pharmacien
 * et Administrateur en héritent (une table par classe, reliées par l'id).
 */
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "utilisateur")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utilisateur extends AbstractEntity {

    private String nom;
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @ToString.Exclude
    @Column(name = "mot_de_passe_hash", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default
    private boolean actif = true;
}
