package com.forceN.gestionpatient.outils;

import com.forceN.gestionpatient.etablissement.entities.Affiliation;
import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.enums.StatutAffiliation;
import com.forceN.gestionpatient.etablissement.enums.TypeEtablissement;
import com.forceN.gestionpatient.etablissement.repository.AffiliationRepository;
import com.forceN.gestionpatient.etablissement.repository.EtablissementRepository;
import com.forceN.gestionpatient.securite.jwt.JwtService;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Outil partagé par les tests : ouvre une session comme le ferait la vraie connexion.
 * Le personnel doit être affilié à un établissement, et son jeton doit désigner cet établissement ;
 * le patient reçoit un jeton sans établissement.
 */
@Component
@RequiredArgsConstructor
public class SessionsDeTest {

    private final EtablissementRepository etablissementRepository;
    private final AffiliationRepository affiliationRepository;
    private final JwtService jwtService;

    public Etablissement creerClinique(String nom) {
        return etablissementRepository.saveAndFlush(Etablissement.builder()
                .nom(nom)
                .type(TypeEtablissement.CLINIQUE)
                .ville("Thiès")
                .build());
    }

    /** Affilie la personne à l'établissement avec le rôle de son compte (si ce n'est pas déjà fait). */
    public Affiliation affilier(Utilisateur utilisateur, Etablissement etablissement) {
        return affiliationRepository.findByUtilisateurIdAndStatut(utilisateur.getId(), StatutAffiliation.ACTIVE).stream()
                .filter(affiliation -> affiliation.getEtablissement().getId().equals(etablissement.getId()))
                .findFirst()
                .orElseGet(() -> affiliationRepository.saveAndFlush(Affiliation.builder()
                        .utilisateur(utilisateur)
                        .etablissement(etablissement)
                        .role(utilisateur.getRole())
                        .build()));
    }

    /** Le jeton qu'aurait reçu la personne en se connectant : avec l'établissement pour le personnel, sans pour un patient. */
    public String jeton(Utilisateur utilisateur, Etablissement etablissement) {
        if (utilisateur.getRole() == Role.PATIENT) {
            return jwtService.genererJeton(utilisateur.getEmail());
        }
        affilier(utilisateur, etablissement);
        return jwtService.genererJeton(utilisateur.getEmail(), etablissement.getId());
    }
}
