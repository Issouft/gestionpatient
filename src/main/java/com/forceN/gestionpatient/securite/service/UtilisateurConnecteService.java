package com.forceN.gestionpatient.securite.service;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.etablissement.service.AffiliationService;
import com.forceN.gestionpatient.securite.exception.SecuriteErreur;
import com.forceN.gestionpatient.securite.jwt.JetonLu;
import com.forceN.gestionpatient.securite.permission.RolePermissions;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Charge un compte pour Spring Security : à la connexion (email + mot de passe) et à chaque requête (jeton).
 * Les droits sont recalculés à chaque requête à partir de la base, jamais lus dans le jeton.
 */
@Service
@RequiredArgsConstructor
public class UtilisateurConnecteService implements UserDetailsService {

    private static final String SESSION_PLUS_VALABLE = "Votre session n'est plus valable. Reconnectez-vous.";

    private final UtilisateurRepository utilisateurRepository;
    private final AffiliationService affiliationService;

    // Utilisée par Spring à la connexion, seulement pour vérifier le mot de passe : aucun droit n'est donné ici.
    @Override
    public UserDetails loadUserByUsername(String email) {
        return utilisateurRepository.findByEmail(email)
                .map(utilisateur -> versCompte(utilisateur, null, List.of()))
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
    }

    /**
     * Utilisée par JwtFiltre à chaque requête. Lance une ApplicationException (401) si le jeton ne doit plus servir :
     * compte supprimé ou désactivé, mot de passe changé depuis, ou accès à l'établissement retiré.
     */
    public CompteAuthentifie chargerPourJeton(JetonLu jeton) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(jeton.email())
                .filter(Utilisateur::isActif)
                .filter(compte -> jetonCreeApresDernierChangement(compte, jeton.emisLe()))
                .orElseThrow(() -> new ApplicationException(SecuriteErreur.NON_AUTHENTIFIE, SESSION_PLUS_VALABLE));

        Integer etablissementId = jeton.etablissementId();

        // Patient et super-administrateur agissent sans établissement : leurs jetons n'en désignent jamais,
        // et leurs droits viennent de leur rôle. Un tel jeton qui désigne un établissement n'a pas été émis par nous.
        Role roleDuCompte = utilisateur.getRole();
        if (roleDuCompte.agitSansEtablissement()) {
            if (etablissementId != null) {
                throw new ApplicationException(SecuriteErreur.NON_AUTHENTIFIE, SESSION_PLUS_VALABLE);
            }
            return versCompte(utilisateur, null, droits(Set.of(roleDuCompte)));
        }

        // Personnel qui n'a pas encore choisi son établissement : connecté, mais sans aucune permission.
        if (etablissementId == null) {
            return versCompte(utilisateur, null, List.of());
        }

        Set<Role> roles = affiliationService.rolesDuPersonnel(utilisateur.getId(), etablissementId);
        if (roles.isEmpty()) {
            throw new ApplicationException(SecuriteErreur.ACCES_ETABLISSEMENT_RETIRE,
                    "Vous n'avez plus accès à cet établissement. Reconnectez-vous pour choisir un autre établissement.");
        }
        return versCompte(utilisateur, etablissementId, droits(roles));
    }

    /**
     * Les droits donnés à Spring Security : les permissions de chaque rôle (vérifiées par les routes avec hasAuthority),
     * plus chaque rôle sous la forme ROLE_xxx, pour information.
     */
    List<GrantedAuthority> droits(Set<Role> roles) {
        List<GrantedAuthority> droits = new ArrayList<>();
        for (Role role : roles) {
            droits.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
            RolePermissions.de(role).forEach(permission -> {
                SimpleGrantedAuthority droit = new SimpleGrantedAuthority(permission.name());
                if (!droits.contains(droit)) {
                    droits.add(droit);
                }
            });
        }
        return droits;
    }

    // La date du jeton est à la seconde près : on compare donc à la seconde près.
    private boolean jetonCreeApresDernierChangement(Utilisateur utilisateur, Instant jetonEmisLe) {
        Instant changement = utilisateur.getMotDePasseModifieLe();
        return changement == null || !jetonEmisLe.isBefore(changement.truncatedTo(ChronoUnit.SECONDS));
    }

    private CompteAuthentifie versCompte(Utilisateur utilisateur, Integer etablissementId, List<GrantedAuthority> droits) {
        return new CompteAuthentifie(utilisateur.getEmail(), utilisateur.getPassword(), utilisateur.isActif(),
                utilisateur.getRole(), etablissementId, List.copyOf(droits));
    }
}
