package com.forceN.gestionpatient.etablissement.entities;

import com.forceN.gestionpatient.commun.AbstractEntity;
import com.forceN.gestionpatient.etablissement.enums.TypeEtablissement;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Un établissement inscrit sur la plateforme (SaaS) : une clinique pour l'instant.
 * Le personnel, les rendez-vous, les dossiers et les médicaments y seront rattachés,
 * pour que chaque établissement ne voie que ses propres données.
 */
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "etablissement")
public class Etablissement extends AbstractEntity {

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeEtablissement type;

    private String adresse;
    private String ville;
    private String telephone;

    // Email de contact de l'établissement (pas un compte de connexion).
    private String email;

    // Un établissement inactif est suspendu : il n'apparaît plus aux patients et son personnel ne peut plus y travailler.
    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;
}
