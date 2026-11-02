package com.forceN.gestionpatient.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Consultation extends AbstractEntity {

    @Column(name = "date_consultation")
    private LocalDateTime date;

    private String observations;
    private String diagnostic;

    // DossierPatient 1 — * Consultation (composition)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_patient_id", nullable = false)
    @ToString.Exclude
    private DossierPatient dossierPatient;

    // Medecin 1 — * Consultation ("rédige")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    @ToString.Exclude
    private Medecin medecin;

    // RendezVous 1 — 0..1 Consultation ("donne lieu à")
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "rendez_vous_id", nullable = false, unique = true)
    @ToString.Exclude
    private RendezVous rendezVous;
}
