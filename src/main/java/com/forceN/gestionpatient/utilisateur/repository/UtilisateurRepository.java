package com.forceN.gestionpatient.utilisateur.repository;

import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.forceN.gestionpatient.utilisateur.enums.Role;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    Optional<Utilisateur> findByEmail(String email);

    // Requête générée par Spring à partir du nom de la méthode. Sert à refuser un email déjà pris.
    boolean existsByEmail(String email);

    // Sert au démarrage : existe-t-il déjà un compte avec ce rôle ?
    boolean existsByRole(Role role);

}
