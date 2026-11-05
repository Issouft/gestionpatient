package com.forceN.gestionpatient.utilisateur.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
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

    /** Recopie les champs communs dans le DTO de réponse, sans le mot de passe. */
    private UtilisateurDto convertiUtilisateurDto(Utilisateur utilisateur) {
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
