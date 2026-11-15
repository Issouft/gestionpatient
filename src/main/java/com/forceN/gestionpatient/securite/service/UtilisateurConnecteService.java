package com.forceN.gestionpatient.securite.service;

import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/** Charge un compte pour Spring Security : à la connexion (email + mot de passe) et à chaque requête (jeton). */
@Service
@RequiredArgsConstructor
public class UtilisateurConnecteService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    // Utilisée par Spring à la connexion, pour vérifier le mot de passe.
    @Override
    public UserDetails loadUserByUsername(String email) {
        return utilisateurRepository.findByEmail(email)
                .map(this::versUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
    }

    /**
     * Utilisée par JwtFiltre à chaque requête. Rien si le compte n'existe plus,
     * ou si le jeton a été créé avant le dernier changement de mot de passe.
     */
    public Optional<UserDetails> chargerPourJeton(String email, Instant jetonEmisLe) {
        return utilisateurRepository.findByEmail(email)
                .filter(utilisateur -> jetonCreeApresDernierChangement(utilisateur, jetonEmisLe))
                .map(this::versUserDetails);
    }

    // La date du jeton est à la seconde près : on compare donc à la seconde près.
    private boolean jetonCreeApresDernierChangement(Utilisateur utilisateur, Instant jetonEmisLe) {
        Instant changement = utilisateur.getMotDePasseModifieLe();
        return changement == null || !jetonEmisLe.isBefore(changement.truncatedTo(ChronoUnit.SECONDS));
    }

    private UserDetails versUserDetails(Utilisateur utilisateur) {
        return User.withUsername(utilisateur.getEmail())
                .password(utilisateur.getPassword())
                .roles(utilisateur.getRole().name())
                .disabled(!utilisateur.isActif())
                .build();
    }
}
