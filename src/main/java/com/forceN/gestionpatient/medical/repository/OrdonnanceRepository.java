package com.forceN.gestionpatient.medical.repository;

import com.forceN.gestionpatient.medical.entities.Ordonnance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface OrdonnanceRepository extends JpaRepository<Ordonnance, Integer> {

    // Une consultation ne donne lieu qu'à une seule ordonnance.
    boolean existsByConsultationId(Integer consultationId);

    Optional<Ordonnance> findByConsultationId(Integer consultationId);

    // Pour les rapports : ordonnances émises dans la période [debut, fin[.
    @Query("select count(o) from Ordonnance o where o.dateEmission >= :debut and o.dateEmission < :fin")
    long compterEntre(Instant debut, Instant fin);
}
