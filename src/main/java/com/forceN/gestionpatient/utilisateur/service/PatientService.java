package com.forceN.gestionpatient.utilisateur.service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.etablissement.dto.EtablissementPublicDto;
import com.forceN.gestionpatient.etablissement.service.AffiliationService;
import com.forceN.gestionpatient.etablissement.service.EtablissementService;
import com.forceN.gestionpatient.utilisateur.dto.PatientDto;
import com.forceN.gestionpatient.utilisateur.dto.PatientInscriptionDto;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.exception.UtilisateurErreur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

/** Logique métier des patients : l'inscription, et les établissements (cliniques) qu'ils rejoignent. */
@Service
@RequiredArgsConstructor
public class PatientService {
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurService utilisateurService;
    private final EtablissementService etablissementService;
    private final AffiliationService affiliationService;

    /**
     * Crée un compte patient et le renvoie sans mot de passe.
     * Refuse un email déjà utilisé (409). Le rôle PATIENT est fixé par @PrePersist dans Patient.
     */
    
    // Tout ou rien : les deux INSERT (utilisateur puis patient) sont validés ensemble, ou annulés ensemble en cas d'erreur.
    @Transactional
    public PatientDto inscrire(PatientInscriptionDto dto) {
        // Email normalisé (sans espaces, en minuscules) : "Awa@Exemple.sn" et "awa@exemple.sn"
        // désignent le même compte. Sans cela, un même email pourrait créer deux comptes.
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        if (utilisateurRepository.existsByEmail(email)) {
            throw new ApplicationException(UtilisateurErreur.EMAIL_DEJA_UTILISE,
                    "Connectez-vous ou choisissez un autre email");
        }
        // Clinique choisie dans l'annuaire : vérifiée AVANT de créer le compte (404 si inconnue ou désactivée).
        if (dto.getEtablissementId() != null) {
            etablissementService.exigerActif(dto.getEtablissementId());
        }

        Patient patient = new Patient();

        patient.setNom(dto.getNom());
        patient.setPrenom(dto.getPrenom());
        patient.setEmail(email);
        // Seule l'empreinte BCrypt est stockée, jamais le mot de passe en clair.
        patient.setPassword(passwordEncoder.encode(dto.getMotDePasse()));
        patient.setDateNaissance(dto.getDateNaissance());
        patient.setSexe(dto.getSexe());
        patient.setTelephone(dto.getTelephone());
        patient.setAdresse(dto.getAdresse());
        // save renvoie le patient avec l'id généré par la base : c'est lui qu'on convertit.
        // La conversion est dans UtilisateurService : une seule façon de transformer un compte en DTO.
        Patient enregistre = utilisateurRepository.save(patient);
        if (dto.getEtablissementId() != null) {
            affiliationService.rejoindreEnPatient(enregistre, dto.getEtablissementId());
        }
        return utilisateurService.convertiPatientDto(enregistre);
    }

    /**
     * Le patient connecté rejoint un établissement : celui-ci pourra le voir (rendez-vous, dossier...).
     * Adhésion directe, sans validation. Ne fait rien s'il en fait déjà partie.
     * 404 si l'établissement n'existe pas ou est désactivé ; 403 ADHESION_SUSPENDUE si l'établissement l'a suspendu.
     */
    @Transactional
    public void rejoindre(String email, Integer etablissementId) {
        affiliationService.rejoindreEnPatient(utilisateurService.trouverCompteConnecte(email), etablissementId);
    }

    /** Les établissements que le patient connecté a rejoints (actifs, adhésion active), triés par nom. */
    @Transactional(readOnly = true)
    public List<EtablissementPublicDto> mesEtablissements(String email) {
        return affiliationService.etablissementsDuPatient(utilisateurService.trouverCompteConnecte(email).getId());
    }
}
