package com.forceN.gestionpatient.etablissement.repository;

import com.forceN.gestionpatient.etablissement.entities.Affiliation;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AffiliationRepository extends JpaRepository<Affiliation, Integer> {

    List<Affiliation> findByUtilisateurIdAndStatut(Integer utilisateurId, StatutAffiliation statut);

    // Sert à refuser une affiliation en double avant que la base ne le fasse.
    boolean existsByUtilisateurIdAndEtablissementIdAndRole(Integer utilisateurId, Integer etablissementId, Role role);

    /**
     * Les affiliations qui donnent vraiment accès : affiliation ACTIVE dans un établissement actif.
     * « join fetch » charge l'établissement dans la même requête (son nom est affiché à la connexion).
     */
    @Query("""
            select a from Affiliation a join fetch a.etablissement e
            where a.utilisateur.id = :utilisateurId
              and a.statut = com.forceN.gestionpatient.etablissement.enums.StatutAffiliation.ACTIVE
              and e.actif = true
            order by e.nom, e.id
            """)
    List<Affiliation> trouverAccesActifs(Integer utilisateurId);
}
