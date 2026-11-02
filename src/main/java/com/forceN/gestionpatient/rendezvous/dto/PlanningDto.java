package com.forceN.gestionpatient.rendezvous.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.forceN.gestionpatient.rendezvous.entities.Creneau;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PlanningDto {

    private Medecin medecin;

    private List<Creneau> creneaux = new ArrayList<>();

    public void ajouterCreneau(Creneau creneau) {
        creneaux.add(creneau);
        creneau.setPlanning(null);
    }

    public void supprimerCreneau(Creneau creneau) {
        creneaux.remove(creneau);
        creneau.setPlanning(null);
    }
}
