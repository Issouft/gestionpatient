package com.forceN.gestionpatient.securite.service;

import com.forceN.gestionpatient.securite.permission.RolePermissions;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
                .authorities(droits(utilisateur.getRole()))
                .disabled(!utilisateur.isActif())
                .build();
    }

    /**
     * Les droits donnés à Spring Security : les permissions du rôle (vérifiées par les routes avec hasAuthority),
     * plus le rôle lui-même sous la forme ROLE_xxx, pour information.
     */
    private List<GrantedAuthority> droits(Role role) {
        List<GrantedAuthority> droits = new ArrayList<>();
        droits.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        RolePermissions.de(role).forEach(permission -> droits.add(new SimpleGrantedAuthority(permission.name())));
        return droits;
    }
}
