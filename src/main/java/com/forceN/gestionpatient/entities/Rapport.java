package com.forceN.gestionpatient.entities;

import com.forceN.gestionpatient.entities.enums.TypeRapport;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDate periodeDebut;

    @Column(name = "periode_fin")
    private LocalDate periodeFin;

    @Column(name = "date_generation")
    private LocalDateTime dateGeneration;

    // Attribut conservé de ton ancienne classe (absent du diagramme)
    @Column(name = "resume_rapport")
    private String resume;

    // Administrateur 1 — * Rapport ("génère")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "administrateur_id", nullable = false)
    @ToString.Exclude
    private Administrateur administrateur;
}
