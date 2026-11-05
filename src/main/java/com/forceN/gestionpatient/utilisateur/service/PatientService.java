package com.forceN.gestionpatient.utilisateur.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.utilisateur.dto.PatientDto;
import com.forceN.gestionpatient.utilisateur.dto.PatientInscriptionDto;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.exception.UtilisateurErreur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

/** Logique métier des patients. Pour l'instant : l'inscription. */
@Service
@RequiredArgsConstructor
public class PatientService {
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

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
        return convertiPatientDTO(utilisateurRepository.save(patient));
    }

    /** Recopie l'entité dans le DTO de réponse, sans le mot de passe. */
    private PatientDto convertiPatientDTO(Patient patient) {
        PatientDto dto = new PatientDto();
        dto.setId(patient.getId());
        dto.setNom(patient.getNom());
        dto.setPrenom(patient.getPrenom());
        dto.setEmail(patient.getEmail());
        dto.setDateNaissance(patient.getDateNaissance());
        dto.setSexe(patient.getSexe());
        dto.setTelephone(patient.getTelephone());
        dto.setRole(patient.getRole());
        dto.setActif(patient.isActif());
        dto.setAdresse(patient.getAdresse());
        return dto;
    }
}
