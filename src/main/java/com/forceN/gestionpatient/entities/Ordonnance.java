package com.forceN.gestionpatient.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Ordonnance extends AbstractEntity {

    @Column(name = "date_emission")
    private LocalDate dateEmission;

    // Consultation 1 — 0..1 Ordonnance ("génère")
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    @ToString.Exclude
    private Consultation consultation;

    // Composition : Ordonnance 1 — 1..* LigneOrdonnance ("contient")
    @OneToMany(mappedBy = "ordonnance", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    private List<LigneOrdonnance> lignes = new ArrayList<>();

    public void ajouterLigne(LigneOrdonnance ligne) {
        lignes.add(ligne);
        ligne.setOrdonnance(this);
    }
}
