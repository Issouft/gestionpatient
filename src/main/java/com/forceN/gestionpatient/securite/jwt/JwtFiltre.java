package com.forceN.gestionpatient.securite.jwt;

import com.forceN.gestionpatient.securite.service.UtilisateurConnecteService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Identifie l'utilisateur à partir du jeton. Sans jeton valide, la requête continue
 * sans identité, et Spring Security refuse ensuite les routes protégées.
 */
@RequiredArgsConstructor
public class JwtFiltre extends OncePerRequestFilter {

    private static final String PREFIXE = "Bearer ";

    private final JwtService jwtService;
    private final UtilisateurConnecteService utilisateurConnecteService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String entete = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (entete != null && entete.startsWith(PREFIXE)) {
            jwtService.lire(entete.substring(PREFIXE.length())).ifPresent(this::authentifier);
        }
        chain.doFilter(request, response);
    }

    // L'utilisateur est relu en base à chaque requête : un compte désactivé, ou dont le mot de passe
    // a changé après la création du jeton, perd son accès sans attendre la fin du jeton.
    private void authentifier(JetonLu jeton) {
        utilisateurConnecteService.chargerPourJeton(jeton.email(), jeton.emisLe())
                .filter(UserDetails::isEnabled)
                .ifPresent(utilisateur -> SecurityContextHolder.getContext().setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                utilisateur, null, utilisateur.getAuthorities())));
    }
}
