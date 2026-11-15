package com.forceN.gestionpatient.medical.entities;

import com.forceN.gestionpatient.commun.AbstractEntity;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.Pharmacien;
import com.forceN.gestionpatient.medical.enums.StatutDemande;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Un patient demande si un médicament est disponible, en une certaine quantité.
 * Parcours : EN_ATTENTE → VALIDEE (puis retrait confirmé par le pharmacien) ou REFUSEE.
 */
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
    private Instant dateDemande;

    // "default 1" : la colonne peut être ajoutée à une table qui contient déjà des demandes.
    @Column(nullable = false, columnDefinition = "integer default 1 not null")
    @Builder.Default
    private int quantite = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    private String reponse;

    // Renseignée quand le pharmacien confirme que le patient a pris le produit. C'est à ce moment que le stock baisse.
    // Un champ plutôt qu'un nouveau statut : Hibernate ne met pas à jour la liste des statuts autorisés d'une table existante.
    @Column(name = "date_retrait")
    private Instant dateRetrait;

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

    public void valider(Pharmacien pharmacien, String reponse) {
        this.statut = StatutDemande.VALIDEE;
        this.pharmacien = pharmacien;
        this.reponse = reponse;
    }

    public void refuser(Pharmacien pharmacien, String reponse) {
        this.statut = StatutDemande.REFUSEE;
        this.pharmacien = pharmacien;
        this.reponse = reponse;
    }

    public boolean estRetiree() {
        return dateRetrait != null;
    }
}
