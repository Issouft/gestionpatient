package com.forceN.gestionpatient.securite.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.securite.entities.JetonActivation;
import com.forceN.gestionpatient.securite.exception.SecuriteErreur;
import com.forceN.gestionpatient.securite.repository.JetonActivationRepository;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

/**
 * Crée et vérifie les jetons envoyés par email : invitation du personnel (48 h)
 * et réinitialisation du mot de passe oublié (1 h).
 */
@Service
@RequiredArgsConstructor
public class JetonActivationService {

    // Durée de validité du lien d'invitation.
    private static final Duration DUREE_VALIDITE = Duration.ofHours(48);

    // SecureRandom et pas Random : ses valeurs sont imprévisibles, indispensable pour un secret.
    private final SecureRandom aleatoire = new SecureRandom();

    private final JetonActivationRepository jetonActivationRepository;

    private final PasswordEncoder passwordEncoder;

    // Plus court que l'invitation : ce lien donne accès à un compte déjà actif.
    private static final Duration DUREE_REINITIALISATION = Duration.ofHours(1);

    private final UtilisateurRepository utilisateurRepository;
    private final EmailService emailService;

    /**
     * Crée un jeton pour cet utilisateur, enregistre son empreinte
     * et renvoie le jeton en clair (à mettre dans le lien de l'email).
     */
    @Transactional
    public String creerJeton(Utilisateur utilisateur) {
        return creerJeton(utilisateur, DUREE_VALIDITE);
    }

    /** Même chose, avec une durée de validité choisie. */
    @Transactional
    public String creerJeton(Utilisateur utilisateur, Duration duree) {
        // 0. Désactiver les anciens liens : un seul lien valide à la fois.
        annulerJetons(utilisateur);

        // 1. Fabrique un jeton aléatoire, en clair.
        String jeton = genererJeton();

        // 2. Prépare la ligne à enregistrer en base.
        JetonActivation jetonActivation = new JetonActivation();
        jetonActivation.setJetonHash(hacher(jeton)); // on stocke l'empreinte, jamais le jeton
        jetonActivation.setUtilisateur(utilisateur); // le compte à activer
        jetonActivation.setExpiration(Instant.now().plus(duree)); // maintenant + la durée choisie

        // 3. Enregistre en base.
        jetonActivationRepository.save(jetonActivation);

        // 4. Renvoie le jeton en clair : c'est lui qui ira dans le lien de l'email.
        return jeton;

    }

    /**
     * Rend inutilisables tous les liens encore valables de cet utilisateur.
     * Sert avant un nouveau lien, et quand l'administrateur désactive un compte
     * (sinon un lien d'invitation en attente pourrait le réactiver).
     */
    @Transactional
    public void annulerJetons(Utilisateur utilisateur) {
        List<JetonActivation> jetons = jetonActivationRepository.findByUtilisateurAndUtiliseFalse(utilisateur);
        for (JetonActivation jeton : jetons) {
            jeton.setUtilise(true);
        }
    }

    /**
     * Active le compte lié au jeton avec le mot de passe choisi par la personne.
     * Refuse un jeton inconnu, expiré ou déjà utilisé, toujours avec la même erreur.
     */
    // Pas de save : le jeton et l'utilisateur, lus en base, sont enregistrés à la fin de la transaction.
    @Transactional
    public void activer(String jeton, String motDePasse) {
        // 1. Retrouver le jeton et vérifier qu'il est encore valable
        JetonActivation jetonActivation = verifierJeton(jeton, "Demandez un nouveau lien à l'administrateur.");

        // 2. Donner le mot de passe (haché) et activer le compte
        Utilisateur utilisateur = jetonActivation.getUtilisateur();
        utilisateur.setPassword(passwordEncoder.encode(motDePasse));
        utilisateur.setActif(true);

        // 3. Le lien ne doit plus resservir
        jetonActivation.setUtilise(true);
    }

    /**
     * Envoie un lien de réinitialisation si l'email correspond à un compte actif.
     * Ne dit jamais si le compte existe : sinon n'importe qui pourrait tester des emails.
     */
    @Transactional
    public void demanderReinitialisation(String email) {
        utilisateurRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                // Un compte inactif ne reçoit rien : un compte désactivé ne doit pas pouvoir revenir seul.
                .filter(Utilisateur::isActif)
                .ifPresent(utilisateur -> {
                    String jeton = creerJeton(utilisateur, DUREE_REINITIALISATION);
                    emailService.envoyerReinitialisation(utilisateur.getEmail(), utilisateur.getPrenom(), jeton);
                });
    }

    /** Change le mot de passe avec le jeton reçu par email, sans toucher à l'état actif du compte. */
    @Transactional
    public void reinitialiser(String jeton, String motDePasse) {
        String conseil = "Refaites une demande de mot de passe oublié.";
        JetonActivation jetonActivation = verifierJeton(jeton, conseil);

        Utilisateur utilisateur = jetonActivation.getUtilisateur();
        // Compte désactivé entre la demande et le clic, ou lien d'invitation d'un compte jamais activé : refusé.
        if (!utilisateur.isActif()) {
            throw new ApplicationException(SecuriteErreur.JETON_INVALIDE, conseil);
        }
        utilisateur.setPassword(passwordEncoder.encode(motDePasse));
        jetonActivation.setUtilise(true);
    }

    /** Retrouve le jeton par son empreinte ; refuse un jeton inconnu, expiré ou déjà utilisé, avec la même erreur. */
    private JetonActivation verifierJeton(String jeton, String conseil) {
        JetonActivation jetonActivation = jetonActivationRepository.findByJetonHash(hacher(jeton))
                .orElseThrow(() -> new ApplicationException(SecuriteErreur.JETON_INVALIDE, conseil));

        if (jetonActivation.isUtilise() || jetonActivation.getExpiration().isBefore(Instant.now())) {
            throw new ApplicationException(SecuriteErreur.JETON_INVALIDE, conseil);
        }
        return jetonActivation;
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
