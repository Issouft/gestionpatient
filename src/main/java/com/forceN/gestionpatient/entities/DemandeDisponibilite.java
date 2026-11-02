package com.forceN.gestionpatient.entities;

import com.forceN.gestionpatient.entities.enums.StatutDemande;
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
@Table(name = "demande_disponibilite")
public class DemandeDisponibilite extends AbstractEntity {

    @Column(name = "date_demande")
    private LocalDateTime dateDemande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    private String reponse;

    // Patient 1 — * DemandeDisponibilite ("soumet")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @ToString.Exclude
    private Patient patient;

    // DemandeDisponibilite * — 1 Medicament ("concerne")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medicament_id", nullable = false)
    @ToString.Exclude
    private Medicament medicament;

    // Pharmacien 1 — * DemandeDisponibilite ("traite") : renseigné au traitement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacien_id")
    @ToString.Exclude
    private Pharmacien pharmacien;

    /*
    public void valider(String reponse) {
        this.statut = StatutDemande.VALIDEE;
        this.reponse = reponse;
    }

    public void refuser(String reponse) {
        this.statut = StatutDemande.REFUSEE;
        this.reponse = reponse;
    }

     */
}
