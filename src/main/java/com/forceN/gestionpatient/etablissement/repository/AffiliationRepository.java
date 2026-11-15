package com.forceN.gestionpatient.etablissement.repository;

import com.forceN.gestionpatient.etablissement.entities.Affiliation;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
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

    // Les affiliations d'une personne dans un établissement, pour ces rôles (pour les suspendre ou les réactiver).
    List<Affiliation> findByUtilisateurIdAndEtablissementIdAndRoleIn(Integer utilisateurId, Integer etablissementId,
            Collection<Role> roles);

    // Toutes les affiliations d'un établissement pour ces rôles (pour afficher le statut de chacun dans la liste).
    List<Affiliation> findByEtablissementIdAndRoleIn(Integer etablissementId, Collection<Role> roles);

    // Vrai si la personne fait partie de l'établissement avec l'un de ces rôles, quel que soit le statut.
    boolean existsByUtilisateurIdAndEtablissementIdAndRoleIn(Integer utilisateurId, Integer etablissementId,
            Collection<Role> roles);

    /** Les comptes rattachés à un établissement avec l'un de ces rôles, triés par nom puis prénom. */
    @Query("""
            select distinct u from Affiliation a join a.utilisateur u
            where a.etablissement.id = :etablissementId and a.role in :roles
            order by u.nom, u.prenom
            """)
    List<Utilisateur> trouverComptes(Integer etablissementId, Collection<Role> roles);
}
