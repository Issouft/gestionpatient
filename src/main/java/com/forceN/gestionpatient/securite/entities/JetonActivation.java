package com.forceN.gestionpatient.securite.entities;

import java.time.Instant;

import com.forceN.gestionpatient.commun.AbstractEntity;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Jeton envoyé par email au personnel créé par l'admin, pour choisir son mot de passe.
 * Valable une seule fois et jusqu'à la date d'expiration.
 */

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "jeton_activation")
public class JetonActivation extends AbstractEntity {

    // Empreinte SHA-256 du jeton : le jeton en clair n'existe que dans l'email.
    @Column(name = "jeton_hash", nullable = false, unique = true)
    private String jetonHash;

    // Le compte à activer. LAZY : l'utilisateur n'est chargé que si on en a besoin.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private Instant expiration;

    // Passe à true après l'activation : le lien ne peut pas resservir.
    private boolean utilise;
}
