package com.forceN.gestionpatient.securite.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forceN.gestionpatient.securite.entities.JetonActivation;

public interface JetonActivationRepository extends JpaRepository<JetonActivation, Integer> {

    // Retrouve le jeton à partir de l'empreinte du jeton reçu dans le lien.
    Optional<JetonActivation> findByJetonHash(String jetonHash);
}
