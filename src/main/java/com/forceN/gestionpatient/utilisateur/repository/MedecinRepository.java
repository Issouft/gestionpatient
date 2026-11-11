package com.forceN.gestionpatient.utilisateur.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forceN.gestionpatient.utilisateur.entities.Medecin;

/** Requêtes propres aux médecins : leurs champs (comme numeroOrdre) n'existent pas dans Utilisateur. */
public interface MedecinRepository extends JpaRepository<Medecin, Integer> {

    // Sert à refuser un numéro d'ordre déjà pris, avant que la base ne le refuse avec une erreur 500.
    boolean existsByNumeroOrdre(String numeroOrdre);
}
