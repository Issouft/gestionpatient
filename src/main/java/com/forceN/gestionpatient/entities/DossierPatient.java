package com.forceN.gestionpatient.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dossier_patient")
public class DossierPatient extends AbstractEntity {

    @Column(name = "antecedents")
    private String antecedents;

    // Patient 1 — 1 DossierPatient ("possède")
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    @ToString.Exclude
    private Patient patient;

    // Secretaire 1 — * DossierPatient ("crée")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secretaire_id")
    @ToString.Exclude
    private Secretaire creePar;

    // Composition : DossierPatient 1 — * Consultation ("contient")
    @OneToMany(mappedBy = "dossierPatient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    private List<Consultation> consultations = new ArrayList<>();

    public void ajouterConsultation(Consultation consultation) {
        consultations.add(consultation);
        consultation.setDossierPatient(this);
    }
}
