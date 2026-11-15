package com.forceN.gestionpatient.utilisateur.repository;

import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.forceN.gestionpatient.utilisateur.enums.Role;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    Optional<Utilisateur> findByEmail(String email);

    // Requête générée par Spring à partir du nom de la méthode. Sert à refuser un email déjà pris.
    boolean existsByEmail(String email);

    // Sert au démarrage : récupère l'administrateur s'il existe, pour savoir s'il est déjà actif.
    Optional<Utilisateur> findFirstByRole(Role role);

    // Tous les comptes dont le rôle est dans la liste, triés par nom puis prénom.
    List<Utilisateur> findByRoleInOrderByNomAscPrenomAsc(Collection<Role> roles);

}
