package com.forceN.gestionpatient.securite.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.securite.entities.JetonActivation;
import com.forceN.gestionpatient.securite.exception.SecuriteErreur;
import com.forceN.gestionpatient.securite.repository.JetonActivationRepository;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;

import lombok.RequiredArgsConstructor;

/** Crée et vérifie les jetons d'activation envoyés par email au personnel. */
@Service
@RequiredArgsConstructor
public class JetonActivationService {

    // Durée de validité du lien d'invitation.
    private static final Duration DUREE_VALIDITE = Duration.ofHours(48);

    // SecureRandom et pas Random : ses valeurs sont imprévisibles, indispensable pour un secret.
    private final SecureRandom aleatoire = new SecureRandom();

    private final JetonActivationRepository jetonActivationRepository;

    private final PasswordEncoder passwordEncoder;

   

    /**
     * Crée un jeton pour cet utilisateur, enregistre son empreinte
     * et renvoie le jeton en clair (à mettre dans le lien de l'email).
     */
    @Transactional
    public String creerJeton(Utilisateur utilisateur) {
        // 1. Fabrique un jeton aléatoire, en clair.
        String jeton = genererJeton();

        // 2. Prépare la ligne à enregistrer en base.
        JetonActivation jetonActivation = new JetonActivation();
        jetonActivation.setJetonHash(hacher(jeton)); // on stocke l'empreinte, jamais le jeton
        jetonActivation.setUtilisateur(utilisateur); // le compte à activer
        jetonActivation.setExpiration(Instant.now().plus(DUREE_VALIDITE)); // maintenant + 48 h

        // 3. Enregistre en base.
        jetonActivationRepository.save(jetonActivation);

        // 4. Renvoie le jeton en clair : c'est lui qui ira dans le lien de l'email.
        return jeton;

    }

        /**
     * Active le compte lié au jeton avec le mot de passe choisi par la personne.
     * Refuse un jeton inconnu, expiré ou déjà utilisé, toujours avec la même erreur.
     */
    // Pas de save : le jeton et l'utilisateur, lus en base, sont enregistrés à la fin de la transaction.
    @Transactional
    public void activer(String jeton, String motDePasse) {
        // 1. Retrouver le jeton grâce à son empreinte, sinon erreur
        JetonActivation jetonActivation = jetonActivationRepository.findByJetonHash(hacher(jeton))
                .orElseThrow(() -> new ApplicationException(SecuriteErreur.JETON_INVALIDE,
                        "Demandez un nouveau lien à l'administrateur"));

        // 2. Refuser s'il a déjà servi OU s'il est expiré
        if (jetonActivation.isUtilise() || jetonActivation.getExpiration().isBefore(Instant.now())) {
            throw new ApplicationException(SecuriteErreur.JETON_INVALIDE,
                    "Demandez un nouveau lien à l'administrateur");
        }

        // 3. Donner le mot de passe (haché) et activer le compte
        Utilisateur utilisateur = jetonActivation.getUtilisateur();
        utilisateur.setPassword(passwordEncoder.encode(motDePasse));
        utilisateur.setActif(true);

        // 4. Le lien ne doit plus resservir
        jetonActivation.setUtilise(true);
    }


    /** 32 octets aléatoires écrits en texte utilisable dans une URL. */
    private String genererJeton() {
        byte[] octets = new byte[32];
        aleatoire.nextBytes(octets);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
    }

    /** Empreinte SHA-256 du jeton, en hexadécimal : c'est elle qu'on stocke en base. */
    private String hacher(String jeton) {
        try {
            byte[] empreinte = MessageDigest.getInstance("SHA-256")
                    .digest(jeton.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(empreinte);
        } catch (NoSuchAlgorithmException e) {
            // N'arrive jamais : SHA-256 est toujours présent dans Java.
            throw new IllegalStateException(e);
        }
    }
}
