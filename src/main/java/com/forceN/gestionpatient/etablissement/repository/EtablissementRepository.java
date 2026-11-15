package com.forceN.gestionpatient.etablissement.repository;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EtablissementRepository extends JpaRepository<Etablissement, Integer> {

    // Le plus ancien établissement : sert d'établissement par défaut au premier administrateur.
    Optional<Etablissement> findFirstByOrderByIdAsc();
}
