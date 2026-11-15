package com.forceN.gestionpatient.etablissement.service;

import com.forceN.gestionpatient.etablissement.dto.AccesEtablissementDto;
import com.forceN.gestionpatient.etablissement.entities.Affiliation;
import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.repository.AffiliationRepository;
import com.forceN.gestionpatient.etablissement.repository.EtablissementRepository;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Où une personne peut-elle travailler, et avec quels rôles ?
 * Les autres domaines (la sécurité notamment) passent par ce service, jamais par les tables du domaine.
 * Seules comptent les affiliations ACTIVE dans un établissement actif.
 */
@Service
@RequiredArgsConstructor
public class AffiliationService {

    private final AffiliationRepository affiliationRepository;
    private final EtablissementRepository etablissementRepository;

    /**
     * Les établissements où la personne travaille, triés par nom, avec ses rôles dans chacun.
     * Les affiliations de patient ne sont pas comptées : un patient agit pour lui-même, sans choisir d'établissement.
     */
    @Transactional(readOnly = true)
    public List<AccesEtablissementDto> accesDuPersonnel(Integer utilisateurId) {
        // LinkedHashMap : garde l'ordre de la requête (par nom). Une personne peut avoir plusieurs rôles au même endroit.
        Map<Integer, AccesEtablissementDto> parEtablissement = new LinkedHashMap<>();
        for (Affiliation affiliation : affiliationRepository.trouverAccesActifs(utilisateurId)) {
            if (affiliation.getRole() == Role.PATIENT) {
                continue;
            }
            Integer id = affiliation.getEtablissement().getId();
            parEtablissement.computeIfAbsent(id, cle -> new AccesEtablissementDto(
                    id, affiliation.getEtablissement().getNom(), EnumSet.noneOf(Role.class)))
                    .roles().add(affiliation.getRole());
        }
        return new ArrayList<>(parEtablissement.values());
    }

    /**
     * Rattache une personne à un établissement avec un rôle. Ne fait rien si c'est déjà le cas.
     * À appeler dans la même transaction que la création du compte : tout ou rien.
     */
    @Transactional
    public void affilier(Utilisateur utilisateur, Integer etablissementId, Role role) {
        if (affiliationRepository.existsByUtilisateurIdAndEtablissementIdAndRole(utilisateur.getId(), etablissementId, role)) {
            return;
        }
        Etablissement etablissement = etablissementRepository.findById(etablissementId)
                .orElseThrow(() -> new IllegalStateException("Établissement introuvable : " + etablissementId));
        affiliationRepository.save(Affiliation.builder()
                .utilisateur(utilisateur)
                .etablissement(etablissement)
                .role(role)
                .build());
    }

    /** Vrai si la personne fait partie de l'établissement avec l'un de ces rôles (affiliation active ou suspendue). */
    @Transactional(readOnly = true)
    public boolean faitPartieDe(Integer utilisateurId, Integer etablissementId, Collection<Role> roles) {
        return affiliationRepository.existsByUtilisateurIdAndEtablissementIdAndRoleIn(utilisateurId, etablissementId, roles);
    }

    /** Les comptes rattachés à l'établissement avec l'un de ces rôles, triés par nom puis prénom. */
    @Transactional(readOnly = true)
    public List<Utilisateur> comptesDe(Integer etablissementId, Collection<Role> roles) {
        return affiliationRepository.trouverComptes(etablissementId, roles);
    }

    /** Les rôles de la personne dans cet établissement. Vide si elle n'y a pas (ou plus) accès. */
    @Transactional(readOnly = true)
    public Set<Role> rolesDuPersonnel(Integer utilisateurId, Integer etablissementId) {
        return accesDuPersonnel(utilisateurId).stream()
                .filter(acces -> acces.etablissementId().equals(etablissementId))
                .findFirst()
                .map(AccesEtablissementDto::roles)
                .orElse(Set.of());
    }
}
