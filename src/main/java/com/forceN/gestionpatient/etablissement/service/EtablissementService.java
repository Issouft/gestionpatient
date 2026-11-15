package com.forceN.gestionpatient.etablissement.service;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.etablissement.dto.EtablissementDto;
import com.forceN.gestionpatient.etablissement.dto.EtablissementPublicDto;
import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.enums.TypeEtablissement;
import com.forceN.gestionpatient.etablissement.exception.EtablissementErreur;
import com.forceN.gestionpatient.etablissement.repository.EtablissementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Les établissements de la plateforme : gérés par le super-administrateur, et présentés au public dans l'annuaire.
 * Ce service ne donne accès à aucune donnée des établissements (personnel, patients, dossiers...) :
 * seulement à leur fiche (nom, type, adresse, contact, actif).
 */
@Service
@RequiredArgsConstructor
public class EtablissementService {

    private final EtablissementRepository etablissementRepository;

    /** Crée un établissement, actif. Son administrateur sera invité ensuite. */
    @Transactional
    public EtablissementDto creer(EtablissementDto dto) {
        return EtablissementDto.fromEntity(etablissementRepository.save(dto.toEntity()));
    }

    /** Tous les établissements, actifs ou non, triés par nom. */
    @Transactional(readOnly = true)
    public List<EtablissementDto> lister() {
        return etablissementRepository.findAll(Sort.by("nom", "id")).stream()
                .map(EtablissementDto::fromEntity)
                .toList();
    }

    /**
     * Désactive un établissement : tout son personnel perd l'accès dès sa requête suivante (voir AffiliationService),
     * et il n'est plus proposé à la connexion. Les données restent en base. Ne fait rien s'il est déjà inactif.
     */
    @Transactional
    public void desactiver(Integer id) {
        trouver(id).setActif(false);
    }

    /** Réactive un établissement : son personnel retrouve l'accès. Ne fait rien s'il est déjà actif. */
    @Transactional
    public void reactiver(Integer id) {
        trouver(id).setActif(true);
    }

    /**
     * L'annuaire public : les établissements actifs, triés par nom, filtrés par type et par ville si on les donne.
     * La ville ignore les majuscules et les espaces autour ("  thiès " trouve "Thiès").
     */
    @Transactional(readOnly = true)
    public List<EtablissementPublicDto> annuaire(TypeEtablissement type, String ville) {
        String villeMinuscules = (ville == null || ville.isBlank()) ? null : ville.trim().toLowerCase(Locale.ROOT);
        return etablissementRepository.trouverActifs(type, villeMinuscules).stream()
                .map(EtablissementPublicDto::fromEntity)
                .toList();
    }

    /**
     * Pour l'annuaire et le parcours du patient : 404 si l'établissement n'existe pas OU s'il est désactivé.
     * Même réponse dans les deux cas : le public n'apprend pas qu'un établissement désactivé existe.
     */
    @Transactional(readOnly = true)
    public void exigerActif(Integer id) {
        etablissementRepository.findByIdAndActifTrue(id).orElseThrow(EtablissementService::introuvable);
    }

    /** Pour les autres modules (ex. inviter un administrateur) : 404 si l'établissement n'existe pas. */
    @Transactional(readOnly = true)
    public void exigerExistant(Integer id) {
        trouver(id);
    }

    /** 404 si l'établissement n'existe pas. */
    Etablissement trouver(Integer id) {
        return etablissementRepository.findById(id).orElseThrow(EtablissementService::introuvable);
    }

    private static ApplicationException introuvable() {
        return new ApplicationException(EtablissementErreur.ETABLISSEMENT_INTROUVABLE,
                "Aucun établissement ne porte cet identifiant");
    }
}
