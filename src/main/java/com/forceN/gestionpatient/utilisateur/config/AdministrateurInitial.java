package com.forceN.gestionpatient.utilisateur.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import com.forceN.gestionpatient.utilisateur.entities.Administrateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Crée le premier administrateur au démarrage, s'il n'en existe aucun.
 * Sans lui, personne ne pourrait créer le personnel. Identifiants lus dans .env.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdministrateurInitial implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    // Le ":" donne une valeur par défaut (vide) : l'application démarre même si .env ne les contient pas.
    @Value("${administrateur.email:}")
    private String email;

    @Value("${administrateur.mot-de-passe:}")
    private String motDePasse;

    // Exécuté une fois par Spring, juste après le démarrage.
    @Override
    public void run(String... args) {
        if (utilisateurRepository.existsByRole(Role.ADMINISTRATEUR)) {
            return; // déjà un administrateur : rien à faire
        }
        if (email.isBlank() || motDePasse.isBlank()) {
            log.warn("Aucun administrateur : renseignez administrateur.email et administrateur.mot-de-passe dans .env");
            return;
        }

        Administrateur administrateur = new Administrateur();
        administrateur.setNom("Administrateur");
        administrateur.setPrenom("Principal");
        administrateur.setEmail(email.trim().toLowerCase(Locale.ROOT));
        administrateur.setPassword(passwordEncoder.encode(motDePasse));
        administrateur.setActif(true);
        utilisateurRepository.save(administrateur);

        log.info("Premier administrateur créé : {}", administrateur.getEmail());
    }
}
