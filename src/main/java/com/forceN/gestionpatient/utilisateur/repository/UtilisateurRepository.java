package com.forceN.gestionpatient.utilisateur.repository;

import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    Optional<Utilisateur> findByEmail(String email);

    // Requête générée par Spring à partir du nom de la méthode. Sert à refuser un email déjà pris.
    boolean existsByEmail(String email);
}
