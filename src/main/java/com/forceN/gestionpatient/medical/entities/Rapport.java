package com.forceN.gestionpatient.medical.entities;

import com.forceN.gestionpatient.commun.AbstractEntity;
import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import com.forceN.gestionpatient.medical.enums.TypeRapport;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;


@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Rapport extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type_rapport")
    private TypeRapport type;

    @Column(name = "periode_debut")
    private Instant periodeDebut;

    @Column(name = "periode_fin")
    private Instant periodeFin;

    @Column(name = "date_generation")
    private Instant dateGeneration;

    // Attribut conservé de ton ancienne classe (absent du diagramme)
    @Column(name = "resume_rapport")
    private String resume;

    // Administrateur 1 — * Rapport ("génère")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "administrateur_id", nullable = false)
    @ToString.Exclude
    private Administrateur administrateur;
}
