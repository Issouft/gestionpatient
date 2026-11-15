package com.forceN.gestionpatient.medical.repository;

import com.forceN.gestionpatient.medical.entities.DemandeDisponibilite;
import com.forceN.gestionpatient.medical.enums.StatutDemande;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DemandeDisponibiliteRepository extends JpaRepository<DemandeDisponibilite, Integer> {

    List<DemandeDisponibilite> findByPatientIdOrderByDateDemandeDesc(Integer patientId);

    List<DemandeDisponibilite> findAllByOrderByDateDemandeDesc();

    List<DemandeDisponibilite> findByStatutOrderByDateDemandeAsc(StatutDemande statut);

    /**
     * Charge la demande en la verrouillant jusqu'à la fin de la transaction.
     * Deux pharmaciens qui traitent la même demande en même temps passent l'un après l'autre :
     * le second voit qu'elle est déjà traitée, et le stock ne baisse qu'une fois.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DemandeDisponibilite d where d.id = :id")
    Optional<DemandeDisponibilite> trouverPourTraitement(Integer id);

    // Pour les rapports : demandes reçues dans la période [debut, fin[, selon leur statut.
    @Query("select count(d) from DemandeDisponibilite d where d.statut = :statut and d.dateDemande >= :debut and d.dateDemande < :fin")
    long compterParStatutEntre(StatutDemande statut, Instant debut, Instant fin);

    // Pour les rapports : retraits confirmés dans la période.
    @Query("select count(d) from DemandeDisponibilite d where d.dateRetrait >= :debut and d.dateRetrait < :fin")
    long compterRetraitsEntre(Instant debut, Instant fin);

    // Pour les rapports : nombre d'unités sorties du stock dans la période.
    @Query("select coalesce(sum(d.quantite), 0) from DemandeDisponibilite d where d.dateRetrait >= :debut and d.dateRetrait < :fin")
    long compterUnitesRetireesEntre(Instant debut, Instant fin);
}
