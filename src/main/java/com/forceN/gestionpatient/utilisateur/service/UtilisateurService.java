package com.forceN.gestionpatient.utilisateur.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.utilisateur.dto.ChangementMotDePasseDto;
import com.forceN.gestionpatient.utilisateur.dto.UtilisateurDto;
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

    /** Recopie les champs communs dans le DTO de réponse, sans le mot de passe. */
    public UtilisateurDto convertiUtilisateurDto(Utilisateur utilisateur) {
        UtilisateurDto dto = new UtilisateurDto();

        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setRole(utilisateur.getRole());
        dto.setActif(utilisateur.isActif());

        return dto;
    }
}
