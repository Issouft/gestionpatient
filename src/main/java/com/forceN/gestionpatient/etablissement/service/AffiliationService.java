package com.forceN.gestionpatient.etablissement.service;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.etablissement.dto.AccesEtablissementDto;
import com.forceN.gestionpatient.etablissement.dto.EtablissementPublicDto;
import com.forceN.gestionpatient.etablissement.entities.Affiliation;
import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.etablissement.exception.EtablissementErreur;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    /**
     * Suspend ou réactive la personne dans cet établissement seulement (pour ces rôles).
     * Ses affiliations dans les autres établissements ne changent pas. Ne fait rien si le statut est déjà le bon.
     */
    @Transactional
    public void changerStatut(Integer utilisateurId, Integer etablissementId, Collection<Role> roles,
            StatutAffiliation statut) {
        // Pas de save() : les affiliations sont suivies par Hibernate, le changement est enregistré à la fin.
        affiliationRepository.findByUtilisateurIdAndEtablissementIdAndRoleIn(utilisateurId, etablissementId, roles)
                .forEach(affiliation -> affiliation.setStatut(statut));
    }

    /**
     * Le statut de chaque personne dans l'établissement (pour ces rôles), par id de compte.
     * ACTIVE si au moins une de ses affiliations l'est, sinon SUSPENDUE.
     */
    @Transactional(readOnly = true)
    public Map<Integer, StatutAffiliation> statutsDans(Integer etablissementId, Collection<Role> roles) {
        Map<Integer, StatutAffiliation> statuts = new HashMap<>();
        for (Affiliation affiliation : affiliationRepository.findByEtablissementIdAndRoleIn(etablissementId, roles)) {
            statuts.merge(affiliation.getUtilisateur().getId(), affiliation.getStatut(),
                    (dejaVu, nouveau) -> dejaVu == StatutAffiliation.ACTIVE ? dejaVu : nouveau);
        }
        return statuts;
    }

    /** Les comptes rattachés à l'établissement avec l'un de ces rôles, triés par nom puis prénom. */
    @Transactional(readOnly = true)
    public List<Utilisateur> comptesDe(Integer etablissementId, Collection<Role> roles) {
        return affiliationRepository.trouverComptes(etablissementId, roles);
    }

    /**
     * Les comptes en activité dans l'établissement avec ce rôle (ex. les médecins de l'annuaire), triés par nom.
     * Sont exclus : les personnes suspendues, les comptes pas encore activés ou désactivés, et tout le monde
     * si l'établissement est désactivé.
     */
    @Transactional(readOnly = true)
    public List<Utilisateur> comptesActifsDe(Integer etablissementId, Role role) {
        return affiliationRepository.trouverComptesActifs(etablissementId, role);
    }

    // ---------- Patients ----------

    /**
     * Le patient rejoint l'établissement (adhésion directe, sans validation). Ne fait rien s'il en fait déjà partie.
     * 404 si l'établissement n'existe pas ou est désactivé ; 403 si l'établissement a suspendu son adhésion :
     * il ne peut pas l'annuler lui-même en rejoignant à nouveau.
     */
    @Transactional
    public void rejoindreEnPatient(Utilisateur patient, Integer etablissementId) {
        Etablissement etablissement = etablissementRepository.findByIdAndActifTrue(etablissementId)
                .orElseThrow(() -> new ApplicationException(EtablissementErreur.ETABLISSEMENT_INTROUVABLE,
                        "Aucun établissement ne porte cet identifiant"));
        Optional<Affiliation> existante = affiliationRepository
                .findByUtilisateurIdAndEtablissementIdAndRole(patient.getId(), etablissementId, Role.PATIENT);
        if (existante.isPresent()) {
            if (!existante.get().estActive()) {
                throw new ApplicationException(EtablissementErreur.ADHESION_SUSPENDUE,
                        "Cet établissement a suspendu votre adhésion. Contactez-le directement.");
            }
            return;
        }
        affiliationRepository.save(Affiliation.builder()
                .utilisateur(patient)
                .etablissement(etablissement)
                .role(Role.PATIENT)
                .build());
    }

    /** Les établissements actifs que le patient a rejoints (adhésion ACTIVE), triés par nom. */
    @Transactional(readOnly = true)
    public List<EtablissementPublicDto> etablissementsDuPatient(Integer patientId) {
        return affiliationRepository.trouverAccesActifs(patientId).stream()
                .filter(affiliation -> affiliation.getRole() == Role.PATIENT)
                .map(affiliation -> EtablissementPublicDto.fromEntity(affiliation.getEtablissement()))
                .toList();
    }

    /**
     * LA règle de confidentialité pour les Parties 2 et 3 : un établissement ne voit un patient (rendez-vous,
     * dossier...) que si ce patient l'a rejoint, que son adhésion est active et que l'établissement est actif.
     */
    @Transactional(readOnly = true)
    public boolean estPatientDe(Integer patientId, Integer etablissementId) {
        return affiliationRepository.aUnAccesActif(patientId, etablissementId, Role.PATIENT);
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
