package com.forceN.gestionpatient.medical.service;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import com.forceN.gestionpatient.utilisateur.entities.Medecin;
import com.forceN.gestionpatient.utilisateur.entities.Patient;
import com.forceN.gestionpatient.utilisateur.entities.Pharmacien;
import com.forceN.gestionpatient.utilisateur.entities.Secretaire;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.exception.UtilisateurErreur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Retrouve le compte de la personne connectée (l'email vient du jeton), avec son vrai type.
 * Les contrôleurs vérifient déjà le rôle avec @PreAuthorize : le type correspond donc toujours,
 * sauf si le compte a été supprimé après la création du jeton (404).
 */
@Service
@RequiredArgsConstructor
public class CompteConnecteService {

    private final UtilisateurRepository utilisateurRepository;

    public Secretaire secretaire(String email) {
        return trouver(email, Secretaire.class);
    }

    public Medecin medecin(String email) {
        return trouver(email, Medecin.class);
    }

    public Patient patient(String email) {
        return trouver(email, Patient.class);
    }

    public Pharmacien pharmacien(String email) {
        return trouver(email, Pharmacien.class);
    }

    public Administrateur administrateur(String email) {
        return trouver(email, Administrateur.class);
    }

    private <T extends Utilisateur> T trouver(String email, Class<T> type) {
        return utilisateurRepository.findByEmail(email)
                .filter(type::isInstance)
                .map(type::cast)
                .orElseThrow(() -> new ApplicationException(UtilisateurErreur.UTILISATEUR_INTROUVABLE,
                        "Ce compte n'existe plus. Reconnectez-vous."));
    }
}
