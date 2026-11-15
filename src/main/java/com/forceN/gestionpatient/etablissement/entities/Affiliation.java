package com.forceN.gestionpatient.etablissement.entities;

import com.forceN.gestionpatient.commun.AbstractEntity;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Lien entre un compte et un établissement : « tel compte a tel rôle dans tel établissement ».
 * Le rôle du compte (Utilisateur.role) dit qui est la personne ; l'affiliation dit où elle agit.
 * Une personne peut avoir plusieurs affiliations : un médecin dans deux cliniques,
 * un patient suivi dans plusieurs cliniques.
 */
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "affiliation",
        // Un même rôle ne s'obtient qu'une fois par établissement.
        uniqueConstraints = @UniqueConstraint(name = "uk_affiliation_utilisateur_etablissement_role",
                columnNames = {"utilisateur_id", "etablissement_id", "role"}))
public class Affiliation extends AbstractEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    @ToString.Exclude
    private Utilisateur utilisateur;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "etablissement_id", nullable = false)
    @ToString.Exclude
    private Etablissement etablissement;

    // Le rôle de la personne dans CET établissement.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutAffiliation statut = StatutAffiliation.ACTIVE;

    public boolean estActive() {
        return statut == StatutAffiliation.ACTIVE;
    }
}
