package com.forceN.gestionpatient.securite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forceN.gestionpatient.securite.entities.JetonActivation;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;

public interface JetonActivationRepository extends JpaRepository<JetonActivation, Integer> {

    // Retrouve le jeton à partir de l'empreinte du jeton reçu dans le lien.
    Optional<JetonActivation> findByJetonHash(String jetonHash);

    // Les jetons encore valables de cette personne, à désactiver avant d'en créer un nouveau.
    List<JetonActivation> findByUtilisateurAndUtiliseFalse(Utilisateur utilisateur);
}
