package com.forceN.gestionpatient.medical.repository;

import com.forceN.gestionpatient.medical.entities.Rapport;
import com.forceN.gestionpatient.rendezvous.enums.StatutRendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface RapportRepository extends JpaRepository<Rapport, Integer> {

    List<Rapport> findAllByOrderByDateGenerationDesc();

    /**
     * Pour le rapport RENDEZ_VOUS : nombre de rendez-vous de la période [debut, fin[ ayant ce statut.
     * La requête lit la table de la Partie 2, sans la modifier.
     */
    @Query("select count(r) from RendezVous r where r.statut = :statut and r.dateHeure >= :debut and r.dateHeure < :fin")
    long compterRendezVous(StatutRendezVous statut, LocalDateTime debut, LocalDateTime fin);
}
