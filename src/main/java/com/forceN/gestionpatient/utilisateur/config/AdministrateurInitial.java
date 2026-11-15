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
import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Crée le premier administrateur au démarrage, s'il n'en existe aucun.
 * Sans lui, personne ne pourrait créer le personnel.
 * Seul son email est dans .env : le compte est créé inactif et l'administrateur reçoit un lien
 * pour choisir lui-même son mot de passe. Les développeurs ne le connaissent donc jamais.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdministrateurInitial implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JetonActivationService jetonActivationService;
    private final EmailService emailService;

    // Le ":" donne une valeur par défaut (vide) : l'application démarre même si .env ne la contient pas.
    @Value("${administrateur.email:}")
    private String email;

    // Exécuté une fois par Spring, juste après le démarrage.
    @Override
    public void run(String... args) {
        Optional<Utilisateur> existant = utilisateurRepository.findFirstByRole(Role.ADMINISTRATEUR);

        if (existant.isPresent() && existant.get().isActif()) {
            return; // l'administrateur a déjà choisi son mot de passe : rien à faire
        }

        Utilisateur administrateur;
        if (existant.isPresent()) {
            // Créé lors d'un démarrage précédent, mais lien jamais utilisé (perdu, expiré, email non parti).
            administrateur = existant.get();
        } else {
            if (email.isBlank()) {
                log.warn("Aucun administrateur : renseignez administrateur.email dans .env, puis redémarrez");
                return;
            }
            administrateur = creerAdministrateur();
        }

        envoyerLien(administrateur);
    }

    /** Crée l'administrateur, inactif, avec un mot de passe aléatoire que personne ne connaît. */
    private Administrateur creerAdministrateur() {
        Administrateur administrateur = new Administrateur();
        administrateur.setNom("Administrateur");
        administrateur.setPrenom("Principal");
        administrateur.setEmail(email.trim().toLowerCase(Locale.ROOT));
        administrateur.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        administrateur.setActif(false);
        Administrateur enregistre = utilisateurRepository.save(administrateur);
        log.info("Premier administrateur créé, en attente d'activation : {}", enregistre.getEmail());
        return enregistre;
    }

    /** Envoie un nouveau lien d'activation (l'ancien ne marche plus). */
    private void envoyerLien(Utilisateur administrateur) {
        String jeton = jetonActivationService.creerJeton(administrateur);
        try {
            emailService.envoyerInvitation(administrateur.getEmail(), administrateur.getPrenom(), jeton);
            log.info("Lien d'activation envoyé à l'administrateur : {}", administrateur.getEmail());
        } catch (MailException exception) {
            // L'application démarre quand même : un nouveau lien partira au prochain démarrage.
            log.warn("Lien d'activation de l'administrateur non envoyé ; il sera renvoyé au prochain démarrage", exception);
        }
    }
}
