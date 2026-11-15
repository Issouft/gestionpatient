package com.forceN.gestionpatient.utilisateur.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forceN.gestionpatient.etablissement.service.AffiliationService;
import com.forceN.gestionpatient.etablissement.service.EtablissementService;
import com.forceN.gestionpatient.utilisateur.dto.MedecinPublicDto;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import com.forceN.gestionpatient.utilisateur.enums.Role;

import lombok.RequiredArgsConstructor;

/**
 * La partie de l'annuaire public qui concerne les comptes : les médecins d'un établissement.
 * Passe par EtablissementService et AffiliationService (règle des modules), jamais par leurs tables.
 */
@Service
@RequiredArgsConstructor
public class AnnuaireService {

    private final EtablissementService etablissementService;
    private final AffiliationService affiliationService;

    /**
     * Les médecins en activité dans l'établissement, triés par nom, filtrés par spécialité si on la donne
     * (sans tenir compte des majuscules ni des espaces autour).
     * 404 si l'établissement n'existe pas ou est désactivé.
     */
    @Transactional(readOnly = true)
    public List<MedecinPublicDto> medecins(Integer etablissementId, String specialite) {
        etablissementService.exigerActif(etablissementId);
        String recherchee = (specialite == null || specialite.isBlank()) ? null : normaliser(specialite);
        return affiliationService.comptesActifsDe(etablissementId, Role.MEDECIN).stream()
                // Le rôle MEDECIN garantit un Medecin ; le test protège quand même contre une donnée incohérente.
                .filter(Medecin.class::isInstance)
                .map(Medecin.class::cast)
                .filter(medecin -> recherchee == null
                        || (medecin.getSpecialite() != null && normaliser(medecin.getSpecialite()).equals(recherchee)))
                .map(MedecinPublicDto::fromEntity)
                .toList();
    }

    private static String normaliser(String texte) {
        return texte.trim().toLowerCase(Locale.ROOT);
    }
}
