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
public class Planning extends AbstractEntity {

    // Medecin 1 — 1 Planning ("possède")
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false, unique = true)
    @ToString.Exclude
    private Medecin medecin;

    // Composition : Planning 1 — * Creneau ("contient")
    @OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    private List<Creneau> creneaux = new ArrayList<>();

    public void ajouterCreneau(Creneau creneau) {
        creneaux.add(creneau);
        creneau.setPlanning(this);
    }

    public void supprimerCreneau(Creneau creneau) {
        creneaux.remove(creneau);
        creneau.setPlanning(null);
    }
}
