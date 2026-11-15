package com.forceN.gestionpatient.utilisateur.config;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.forceN.gestionpatient.securite.service.EmailService;
import com.forceN.gestionpatient.securite.service.JetonActivationService;
import com.forceN.gestionpatient.utilisateur.entities.SuperAdministrateur;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Crée le super-administrateur au démarrage, s'il n'en existe aucun. Sans lui, personne ne pourrait créer
 * les établissements ni inviter leurs administrateurs.
 * Seul son email est dans .env (super-administrateur.email) : le compte est créé inactif et il reçoit un lien
 * pour choisir lui-même son mot de passe. Les développeurs ne le connaissent donc jamais.
 * Il n'appartient à aucun établissement et n'a accès à aucune donnée des cliniques (voir RolePermissions).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdministrateurInitial implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JetonActivationService jetonActivationService;
    private final EmailService emailService;

    // Le ":" donne une valeur par défaut (vide) : l'application démarre même si .env ne la contient pas.
    @Value("${super-administrateur.email:}")
    private String email;

    // Exécuté une fois par Spring, juste après le démarrage.
    @Override
    public void run(String... args) {
        Optional<Utilisateur> existant = utilisateurRepository.findFirstByRole(Role.SUPER_ADMINISTRATEUR);

        if (existant.isPresent() && existant.get().isActif()) {
            return; // le super-administrateur a déjà choisi son mot de passe : rien à faire
        }

        Utilisateur superAdministrateur;
        if (existant.isPresent()) {
            // Créé lors d'un démarrage précédent, mais lien jamais utilisé (perdu, expiré, email non parti).
            superAdministrateur = existant.get();
        } else {
            if (email.isBlank()) {
                log.warn("Aucun super-administrateur : renseignez super-administrateur.email dans .env, puis redémarrez");
                return;
            }
            String emailNormalise = email.trim().toLowerCase(Locale.ROOT);
            if (utilisateurRepository.existsByEmail(emailNormalise)) {
                // Sans ce contrôle, l'email unique en base ferait planter le démarrage.
                log.error("Super-administrateur non créé : {} appartient déjà à un autre compte. "
                        + "Choisissez une autre adresse pour super-administrateur.email dans .env", emailNormalise);
                return;
            }
            superAdministrateur = creerSuperAdministrateur(emailNormalise);
        }

        envoyerLien(superAdministrateur);
    }

    /** Crée le super-administrateur, inactif, avec un mot de passe aléatoire que personne ne connaît. */
    private SuperAdministrateur creerSuperAdministrateur(String emailNormalise) {
        SuperAdministrateur superAdministrateur = new SuperAdministrateur();
        superAdministrateur.setNom("Plateforme");
        superAdministrateur.setPrenom("Super-administrateur");
        superAdministrateur.setEmail(emailNormalise);
        superAdministrateur.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        superAdministrateur.setActif(false);
        SuperAdministrateur enregistre = utilisateurRepository.save(superAdministrateur);
        log.info("Super-administrateur créé, en attente d'activation : {}", enregistre.getEmail());
        return enregistre;
    }

    /** Envoie un nouveau lien d'activation (l'ancien ne marche plus). */
    private void envoyerLien(Utilisateur superAdministrateur) {
        String jeton = jetonActivationService.creerJeton(superAdministrateur);
        try {
            emailService.envoyerInvitation(superAdministrateur.getEmail(), superAdministrateur.getPrenom(), jeton);
            log.info("Lien d'activation envoyé au super-administrateur : {}", superAdministrateur.getEmail());
        } catch (MailException exception) {
            // L'application démarre quand même : un nouveau lien partira au prochain démarrage.
            log.warn("Lien d'activation du super-administrateur non envoyé ; il sera renvoyé au prochain démarrage", exception);
        }
    }
}
