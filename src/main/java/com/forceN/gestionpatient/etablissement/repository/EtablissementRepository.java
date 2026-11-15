package com.forceN.gestionpatient.etablissement.repository;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtablissementRepository extends JpaRepository<Etablissement, Integer> {
}
