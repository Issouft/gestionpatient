package com.forceN.gestionpatient.medical.repository;

import com.forceN.gestionpatient.medical.entities.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface ConsultationRepository extends JpaRepository<Consultation, Integer> {

    // Un rendez-vous ne donne lieu qu'à une seule consultation.
    boolean existsByRendezVousId(Integer rendezVousId);

    // Pour les rapports : consultations de la période [debut, fin[.
    @Query("select count(c) from Consultation c where c.date >= :debut and c.date < :fin")
    long compterEntre(Instant debut, Instant fin);

    // Pour les rapports : nombre de patients différents consultés dans la période.
    @Query("select count(distinct c.dossierPatient.patient.id) from Consultation c where c.date >= :debut and c.date < :fin")
    long compterPatientsEntre(Instant debut, Instant fin);
}
