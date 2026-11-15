package com.forceN.gestionpatient.medical.repository;

import com.forceN.gestionpatient.medical.entities.Medicament;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MedicamentRepository extends JpaRepository<Medicament, Integer> {

    // Un même médicament (nom + dosage) n'est enregistré qu'une fois.
    boolean existsByNomIgnoreCaseAndDosageIgnoreCase(String nom, String dosage);

    List<Medicament> findAllByOrderByNomAscDosageAsc();

    /**
     * Charge le médicament en le verrouillant jusqu'à la fin de la transaction.
     * Deux retraits en même temps attendent leur tour : le stock ne peut pas devenir négatif.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medicament m where m.id = :id")
    Optional<Medicament> trouverPourModifierStock(Integer id);

    // Pour les rapports de stock.
    @Query("select count(m) from Medicament m where m.stock <= m.seuilAlerte")
    long compterSousSeuilAlerte();

    long countByStock(int stock);
}
