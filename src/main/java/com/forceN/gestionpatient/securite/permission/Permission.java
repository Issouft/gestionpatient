package com.forceN.gestionpatient.securite.permission;

/**
 * Ce qu'une personne a le droit de faire. Les routes vérifient une permission, pas un rôle :
 * @PreAuthorize("hasAuthority('CONSULTATION_CREER')").
 * Ainsi, changer les droits d'un rôle, ou ajouter un rôle (infirmier, laborantin...),
 * se fait dans RolePermissions, sans toucher aux contrôleurs.
 *
 * Pour une nouvelle route : réutiliser une permission qui a le même sens, sinon en ajouter une ici
 * et la donner aux rôles concernés dans RolePermissions.
 */
public enum Permission {

    // Partie 1 : comptes
    PERSONNEL_GERER,        // créer, lister, inviter et désactiver le personnel

    // Partie 3 : médical
    DOSSIER_CREER,
    DOSSIER_LIRE,
    CONSULTATION_CREER,
    CONSULTATION_LIRE,
    ORDONNANCE_CREER,
    ORDONNANCE_LIRE,
    MEDICAMENT_GERER,       // ajouter un médicament au stock
    DEMANDE_SOUMETTRE,      // demander un médicament et suivre ses propres demandes
    DEMANDE_TRAITER,        // valider, refuser une demande et confirmer le retrait
    RAPPORT_GERER           // générer et lister les rapports
}
