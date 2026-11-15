package com.forceN.gestionpatient.utilisateur.entities;

import com.forceN.gestionpatient.commun.AbstractEntity;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
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

    // Date du dernier changement de mot de passe. Les jetons de connexion créés avant sont refusés (voir JwtFiltre).
    @JsonIgnore
    @Column(name = "mot_de_passe_modifie_le")
    private Instant motDePasseModifieLe;

    /**
     * Remplace le mot de passe (déjà haché) et note la date du changement :
     * toutes les sessions ouvertes avant sont fermées.
     */
    public void changerMotDePasse(String motDePasseHache) {
        this.password = motDePasseHache;
        this.motDePasseModifieLe = Instant.now();
    }
}
