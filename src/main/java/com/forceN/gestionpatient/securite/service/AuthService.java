package com.forceN.gestionpatient.securite.service;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.etablissement.dto.AccesEtablissementDto;
import com.forceN.gestionpatient.etablissement.service.AffiliationService;
import com.forceN.gestionpatient.securite.dto.ConnexionReponse;
import com.forceN.gestionpatient.securite.dto.ConnexionRequete;
import com.forceN.gestionpatient.securite.exception.SecuriteErreur;
import com.forceN.gestionpatient.securite.jwt.JwtService;
import com.forceN.gestionpatient.utilisateur.entities.Utilisateur;
import com.forceN.gestionpatient.utilisateur.enums.Role;
import com.forceN.gestionpatient.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Connexion et choix de l'établissement.
 * - Un patient reçoit un jeton sans établissement : il agit pour lui-même, dans toutes ses cliniques.
 * - Le personnel qui travaille dans un seul établissement le reçoit directement dans son jeton.
 * - Celui qui travaille dans plusieurs reçoit la liste, puis choisit avec POST /auth/etablissement.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String IDENTIFIANTS_INCORRECTS = "Email ou mot de passe incorrect.";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;
    private final AffiliationService affiliationService;

    public ConnexionReponse connecter(ConnexionRequete requete) {
        if (requete.getEmail() == null || requete.getMotDePasse() == null) {
            throw new ApplicationException(SecuriteErreur.IDENTIFIANTS_INVALIDES, IDENTIFIANTS_INCORRECTS);
        }
        // Même normalisation qu'à l'inscription (PatientService), sinon "Awa@Exemple.sn" serait refusé.
        String email = requete.getEmail().trim().toLowerCase(Locale.ROOT);
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                    email, requete.getMotDePasse()));
        } catch (AuthenticationException exception) {
            // Même message dans tous les cas, pour ne pas révéler quels comptes existent.
            throw new ApplicationException(SecuriteErreur.IDENTIFIANTS_INVALIDES, IDENTIFIANTS_INCORRECTS);
        }

        Utilisateur utilisateur = trouver(email);
        if (utilisateur.getRole() == Role.PATIENT) {
            return new ConnexionReponse(jwtService.genererJeton(email), null, List.of());
        }

        List<AccesEtablissementDto> etablissements = affiliationService.accesDuPersonnel(utilisateur.getId());
        // Un seul établissement : pas de choix à faire, il est mis dans le jeton.
        Integer etablissementId = etablissements.size() == 1 ? etablissements.getFirst().etablissementId() : null;
        return new ConnexionReponse(jwtService.genererJeton(email, etablissementId), etablissementId, etablissements);
    }

    /**
     * Donne un nouveau jeton pour travailler dans l'établissement choisi. Sert aussi à changer d'établissement.
     * 403 si la personne n'y est pas affiliée, si son affiliation est suspendue ou si l'établissement est désactivé ;
     * la réponse est la même si l'établissement n'existe pas, pour ne pas révéler lesquels existent.
     */
    public ConnexionReponse choisirEtablissement(CompteAuthentifie connecte, Integer etablissementId) {
        if (connecte.roleDuCompte() == Role.PATIENT) {
            throw new ApplicationException(SecuriteErreur.ETABLISSEMENT_NON_AUTORISE,
                    "Un patient n'a pas d'établissement à choisir : son jeton lui suffit.");
        }
        Utilisateur utilisateur = trouver(connecte.email());
        List<AccesEtablissementDto> etablissements = affiliationService.accesDuPersonnel(utilisateur.getId());
        boolean autorise = etablissements.stream()
                .anyMatch(acces -> acces.etablissementId().equals(etablissementId));
        if (!autorise) {
            throw new ApplicationException(SecuriteErreur.ETABLISSEMENT_NON_AUTORISE,
                    "Vous n'avez pas accès à cet établissement.");
        }
        return new ConnexionReponse(jwtService.genererJeton(utilisateur.getEmail(), etablissementId),
                etablissementId, etablissements);
    }

    // Le compte existe forcément : il vient d'être authentifié (mot de passe ou jeton).
    private Utilisateur trouver(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(SecuriteErreur.NON_AUTHENTIFIE,
                        "Votre session n'est plus valable. Reconnectez-vous."));
    }
}
