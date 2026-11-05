package com.forceN.gestionpatient.securite.service;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.securite.dto.ConnexionReponse;
import com.forceN.gestionpatient.securite.dto.ConnexionRequete;
import com.forceN.gestionpatient.securite.exception.SecuriteErreur;
import com.forceN.gestionpatient.securite.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public ConnexionReponse connecter(ConnexionRequete requete) {
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                    requete.getEmail(), requete.getMotDePasse()));
        } catch (AuthenticationException exception) {
            // Même message dans tous les cas, pour ne pas révéler quels comptes existent.
            throw new ApplicationException(SecuriteErreur.IDENTIFIANTS_INVALIDES, "Email ou mot de passe incorrect.");
        }
        return new ConnexionReponse(jwtService.genererJeton(requete.getEmail()));
    }
}
