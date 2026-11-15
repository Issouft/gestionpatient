package com.forceN.gestionpatient.securite;

import com.forceN.gestionpatient.securite.permission.Permission;
import com.forceN.gestionpatient.securite.permission.RolePermissions;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/** Test simple, sans démarrer Spring : vérifie la liste des droits de RolePermissions. */
class RolePermissionsTest {

    @Test
    void chaquePermissionEstDonneeAAuMoinsUnRole() {
        // Une permission oubliée bloquerait sa route pour tout le monde (403).
        for (Permission permission : Permission.values()) {
            boolean donnee = Arrays.stream(Role.values())
                    .anyMatch(role -> RolePermissions.de(role).contains(permission));
            assertThat(donnee).as("%s n'est donnée à aucun rôle", permission).isTrue();
        }
    }

    @Test
    void lePatientNePeutQueRejoindreUneCliniqueEtSoumettreSesDemandes() {
        assertThat(RolePermissions.de(Role.PATIENT))
                .containsExactlyInAnyOrder(Permission.ETABLISSEMENT_REJOINDRE, Permission.DEMANDE_SOUMETTRE);
    }

    @Test
    void leSuperAdministrateurNeGereQueLesEtablissements() {
        // Décision du groupe : aucune donnée des cliniques pour le super-administrateur.
        assertThat(RolePermissions.de(Role.SUPER_ADMINISTRATEUR)).containsExactly(Permission.ETABLISSEMENT_GERER);
    }

    @Test
    void seulLAdministrateurGereLePersonnel() {
        for (Role role : Role.values()) {
            assertThat(RolePermissions.de(role).contains(Permission.PERSONNEL_GERER))
                    .as(role.name())
                    .isEqualTo(role == Role.ADMINISTRATEUR);
        }
    }
}
