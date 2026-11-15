package com.forceN.gestionpatient.etablissement.repository;

import com.forceN.gestionpatient.etablissement.entities.Affiliation;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AffiliationRepository extends JpaRepository<Affiliation, Integer> {

    // Les établissements où une personne peut agir : servira à choisir son établissement à la connexion.
    List<Affiliation> findByUtilisateurIdAndStatut(Integer utilisateurId, StatutAffiliation statut);

    // Sert à refuser une affiliation en double avant que la base ne le fasse.
    boolean existsByUtilisateurIdAndEtablissementIdAndRole(Integer utilisateurId, Integer etablissementId, Role role);
}
