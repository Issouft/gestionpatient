package com.forceN.gestionpatient.entities;

import com.forceN.gestionpatient.entities.enums.StatutRendezVous;
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
@Table(name = "rendez_vous")
public class RendezVous extends AbstractEntity {

    @Column(name = "date_heure")
    private LocalDateTime dateHeure;

    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_rendez_vous", nullable = false)
    @Builder.Default
    private StatutRendezVous statut = StatutRendezVous.EN_ATTENTE;

    // Patient 1 — * RendezVous ("prend")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @ToString.Exclude
    private Patient patient;

    // Medecin 1 — * RendezVous ("reçoit")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    @ToString.Exclude
    private Medecin medecin;

    // Secretaire 1 — * RendezVous ("gère") : renseignée quand une secrétaire traite le RDV
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secretaire_id")
    @ToString.Exclude
    private Secretaire secretaire;

    // Creneau 1 — 0..1 RendezVous ("est réservé par") : unique => un créneau ne peut être réservé deux fois.
    // Nullable en base pour pouvoir libérer le créneau quand le RDV est annulé/rejeté.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creneau_id", unique = true)
    @ToString.Exclude
    private Creneau creneau;

    /* public void confirmer() {
        this.statut = StatutRendezVous.CONFIRME;
    }

    public void reporter() {
        this.statut = StatutRendezVous.REPORTE;
    }

    public void rejeter() {
        this.statut = StatutRendezVous.REJETE;
        libererCreneau();
    }

    public void annuler() {
        this.statut = StatutRendezVous.ANNULE;
        libererCreneau();
    }

    private void libererCreneau() {
        if (creneau != null) {
            creneau.setDisponible(true);
            creneau = null;
        }
    }

     */
}
