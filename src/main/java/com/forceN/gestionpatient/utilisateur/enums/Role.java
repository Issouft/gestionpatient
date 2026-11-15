package com.forceN.gestionpatient.utilisateur.enums;

/**
 * Qui est la personne.
 * - Le personnel (MEDECIN, SECRETAIRE, PHARMACIEN, ADMINISTRATEUR) travaille dans des établissements :
 *   ses droits viennent de ses affiliations.
 * - PATIENT et SUPER_ADMINISTRATEUR agissent sans établissement : leurs droits viennent de leur rôle.
 */
public enum Role {
    MEDECIN, SECRETAIRE, PATIENT, PHARMACIEN, ADMINISTRATEUR,
    // Gère la plateforme : les établissements et leurs administrateurs. N'a accès à aucune donnée des cliniques.
    SUPER_ADMINISTRATEUR;

    /** Vrai pour les rôles qui agissent sans établissement : leur jeton n'en désigne jamais. */
    public boolean agitSansEtablissement() {
        return this == PATIENT || this == SUPER_ADMINISTRATEUR;
    }
}
