package com.forceN.gestionpatient.utilisateur.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.utilisateur.dto.ChangementMotDePasseDto;
import com.forceN.gestionpatient.utilisateur.dto.MedecinDto;
import com.forceN.gestionpatient.utilisateur.dto.ModificationProfilDto;
import com.forceN.gestionpatient.utilisateur.dto.PatientDto;
import com.forceN.gestionpatient.utilisateur.dto.UtilisateurDto;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.exception.UtilisateurErreur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

/** Logique métier commune à tous les utilisateurs, quel que soit leur rôle. */
@Service
@RequiredArgsConstructor
public class UtilisateurService {
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Renvoie le profil de l'utilisateur connecté. L'email vient du jeton, jamais du client.
     * 404 si le compte a été supprimé après la création du jeton.
     */
    // Lecture seule : rien n'est modifié, Hibernate n'a pas à surveiller les changements.
    @Transactional(readOnly = true)
    public UtilisateurDto consulterProfil(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
        .orElseThrow(() -> new ApplicationException(UtilisateurErreur.UTILISATEUR_INTROUVABLE, "Ce compte n'existe plus. Reconnectez-vous."));

        return convertiUtilisateurDto(utilisateur);
    }

    /**
     * Change le mot de passe de l'utilisateur connecté.
     * L'ancien est vérifié pour qu'une personne devant un ordinateur resté connecté ne puisse pas voler le compte.
     */
    @Transactional
    public void changerMotDePasse(String email, ChangementMotDePasseDto dto) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(UtilisateurErreur.UTILISATEUR_INTROUVABLE,
                        "Ce compte n'existe plus. Reconnectez-vous."));

        // matches() hache le texte reçu et le compare au hachage enregistré.
        if (!passwordEncoder.matches(dto.getAncienMotDePasse(), utilisateur.getPassword())) {
            throw new ApplicationException(UtilisateurErreur.MOT_DE_PASSE_INCORRECT,
                    "Vérifiez votre ancien mot de passe.");
        }

        utilisateur.setPassword(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        // Pas de save() : dans une transaction, Hibernate enregistre seul les changements de l'entité.
    }

    /**
     * Modifie le profil de l'utilisateur connecté et renvoie le profil à jour.
     * Le téléphone et l'adresse n'existent que pour le patient : pour les autres rôles, ils sont ignorés.
     */
    @Transactional
    public UtilisateurDto modifierProfil(String email, ModificationProfilDto dto) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
        .orElseThrow(() -> new ApplicationException(UtilisateurErreur.UTILISATEUR_INTROUVABLE, "Ce compte n'existe plus. Reconnectez-vous."));

        utilisateur.setNom(dto.getNom().trim());
        utilisateur.setPrenom(dto.getPrenom().trim());

        // instanceof vérifie le type réel du compte et le convertit en Patient d'un coup.
        if (utilisateur instanceof Patient patient) {
            // null = champ absent du corps : on garde l'ancienne valeur.
            if (dto.getTelephone() != null) {
                patient.setTelephone(dto.getTelephone().trim());
            }
            if (dto.getAdresse() != null) {
                patient.setAdresse(dto.getAdresse().trim());
            }
        }
        // Pas de save() : Hibernate enregistre les changements à la fin de la transaction.
        return convertiUtilisateurDto(utilisateur);
    }

    /**
     * Transforme un compte en DTO de réponse, sans le mot de passe.
     * Un patient devient un PatientDto, un médecin un MedecinDto : leurs champs propres sont inclus.
     * Les autres rôles n'ont que les champs communs.
     */
    public UtilisateurDto convertiUtilisateurDto(Utilisateur utilisateur) {
        if (utilisateur instanceof Patient patient) {
            return convertiPatientDto(patient);
        }
        if (utilisateur instanceof Medecin medecin) {
            MedecinDto dto = new MedecinDto();
            dto.setSpecialite(medecin.getSpecialite());
            dto.setNumeroOrdre(medecin.getNumeroOrdre());
            return remplirChampsCommuns(dto, medecin);
        }
        return remplirChampsCommuns(new UtilisateurDto(), utilisateur);
    }

    /** Patient avec ses champs propres (date de naissance, sexe, téléphone, adresse ; l'âge est calculé). */
    public PatientDto convertiPatientDto(Patient patient) {
        PatientDto dto = new PatientDto();
        dto.setDateNaissance(patient.getDateNaissance());
        dto.setSexe(patient.getSexe());
        dto.setTelephone(patient.getTelephone());
        dto.setAdresse(patient.getAdresse());
        return remplirChampsCommuns(dto, patient);
    }

    /** Recopie les champs communs à tous les rôles. <T> : renvoie le même type de DTO que celui reçu. */
    private <T extends UtilisateurDto> T remplirChampsCommuns(T dto, Utilisateur utilisateur) {
        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setRole(utilisateur.getRole());
        dto.setActif(utilisateur.isActif());

        return dto;
    }
}
