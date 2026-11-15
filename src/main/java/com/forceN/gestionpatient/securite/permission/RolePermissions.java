package com.forceN.gestionpatient.securite.permission;

import com.forceN.gestionpatient.utilisateur.enums.Role;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.forceN.gestionpatient.securite.permission.Permission.*;

/**
 * Les permissions de chaque rôle : le seul endroit où sont décidés les droits.
 * Elles reprennent exactement les droits d'avant les permissions (les anciens hasRole).
 * Les droits définitifs de chaque rôle restent à décider en groupe : il suffira de modifier cette liste.
 */
public final class RolePermissions {

    private static final Map<Role, Set<Permission>> PERMISSIONS = new EnumMap<>(Role.class);

    static {
        PERMISSIONS.put(Role.ADMINISTRATEUR, EnumSet.of(PERSONNEL_GERER, RAPPORT_GERER));
        PERMISSIONS.put(Role.SECRETAIRE, EnumSet.of(DOSSIER_CREER, DOSSIER_LIRE));
        PERMISSIONS.put(Role.MEDECIN, EnumSet.of(DOSSIER_LIRE, CONSULTATION_CREER, CONSULTATION_LIRE,
                ORDONNANCE_CREER, ORDONNANCE_LIRE));
        PERMISSIONS.put(Role.PHARMACIEN, EnumSet.of(ORDONNANCE_LIRE, MEDICAMENT_GERER, DEMANDE_TRAITER));
        PERMISSIONS.put(Role.PATIENT, EnumSet.of(ETABLISSEMENT_REJOINDRE, DEMANDE_SOUMETTRE));
        // Décision du groupe : le super-administrateur gère la plateforme, mais ne voit aucune donnée des cliniques.
        // Ne lui donner AUCUNE permission médicale ni de gestion du personnel.
        PERMISSIONS.put(Role.SUPER_ADMINISTRATEUR, EnumSet.of(ETABLISSEMENT_GERER));
    }

    private RolePermissions() {
        // Classe utilitaire : pas d'instance.
    }

    /** Les permissions d'un rôle. Un rôle absent de la liste n'a aucune permission. */
    public static Set<Permission> de(Role role) {
        return PERMISSIONS.getOrDefault(role, Set.of());
    }
}
