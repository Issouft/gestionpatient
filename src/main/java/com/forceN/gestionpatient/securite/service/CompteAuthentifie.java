package com.forceN.gestionpatient.securite.service;

import com.forceN.gestionpatient.utilisateur.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * La personne connectée, telle que Spring Security la garde pendant une requête.
 * Les contrôleurs la reçoivent avec @AuthenticationPrincipal (comme UserDetails, pour ceux qui n'ont besoin que de l'email).
 *
 * @param email           identifiant du compte (le « username » de Spring)
 * @param motDePasseHash  utilisé seulement à la connexion, pour vérifier le mot de passe
 * @param actif           un compte inactif ne peut ni se connecter ni utiliser son jeton
 * @param roleDuCompte    qui est la personne (PATIENT, MEDECIN...)
 * @param etablissementId où elle travaille pendant cette session ; null pour un patient,
 *                        ou tant que le personnel n'a pas choisi
 * @param droits          ses permissions dans cet établissement (voir RolePermissions)
 */
public record CompteAuthentifie(String email, String motDePasseHash, boolean actif, Role roleDuCompte,
                                Integer etablissementId, List<GrantedAuthority> droits) implements UserDetails {

    /** Vrai pour le personnel qui ne travaille encore dans aucun établissement pendant cette session. */
    public boolean doitChoisirUnEtablissement() {
        return roleDuCompte != Role.PATIENT && etablissementId == null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return motDePasseHash;
    }

    @Override
    public boolean isEnabled() {
        return actif;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return droits;
    }

    // Le hash du mot de passe ne doit jamais apparaître dans un log.
    @Override
    public String toString() {
        return "CompteAuthentifie[email=" + email + ", role=" + roleDuCompte + ", etablissementId=" + etablissementId + "]";
    }
}
