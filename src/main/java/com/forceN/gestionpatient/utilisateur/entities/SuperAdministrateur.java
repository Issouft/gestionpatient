package com.forceN.gestionpatient.utilisateur.entities;

import com.forceN.gestionpatient.utilisateur.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Gère la plateforme : crée les établissements et invite leurs administrateurs.
 * Il n'appartient à aucun établissement et n'a accès à aucune donnée des cliniques (dossiers, consultations...).
 */
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "super_administrateur")
public class SuperAdministrateur extends Utilisateur {

    @PrePersist
    void definirRole() {
        setRole(Role.SUPER_ADMINISTRATEUR);
    }
}
