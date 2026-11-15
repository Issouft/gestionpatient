package com.forceN.gestionpatient.medical.repository;

import com.forceN.gestionpatient.medical.entities.DossierPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface DossierPatientRepository extends JpaRepository<DossierPatient, Integer> {

    // Un patient n'a qu'un seul dossier : sert à refuser un deuxième dossier.
    boolean existsByPatientId(Integer patientId);

    Optional<DossierPatient> findByPatientId(Integer patientId);

    // Pour les rapports : dossiers créés dans la période [debut, fin[.
    @Query("select count(d) from DossierPatient d where d.creationDate >= :debut and d.creationDate < :fin")
    long compterCreesEntre(Instant debut, Instant fin);
}
