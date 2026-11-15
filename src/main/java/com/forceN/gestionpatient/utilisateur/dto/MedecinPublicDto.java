package com.forceN.gestionpatient.utilisateur.dto;

import com.forceN.gestionpatient.utilisateur.entities.Medecin;

/**
 * Un médecin tel que le voit le public dans l'annuaire : de quoi le reconnaître et le choisir, rien de plus.
 * Ni email, ni numéro d'ordre, ni téléphone. L'id servira à prendre rendez-vous (Partie 2).
 */
public record MedecinPublicDto(Integer id, String nom, String prenom, String specialite) {

    public static MedecinPublicDto fromEntity(Medecin medecin) {
        return new MedecinPublicDto(medecin.getId(), medecin.getNom(), medecin.getPrenom(), medecin.getSpecialite());
    }
}
