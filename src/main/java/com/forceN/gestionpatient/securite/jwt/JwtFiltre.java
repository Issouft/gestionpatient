package com.forceN.gestionpatient.securite.jwt;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.securite.service.CompteAuthentifie;
import com.forceN.gestionpatient.securite.service.UtilisateurConnecteService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Identifie l'utilisateur à partir du jeton. Sans jeton valide, la requête continue
 * sans identité, et Spring Security refuse ensuite les routes protégées.
 */
@RequiredArgsConstructor
public class JwtFiltre extends OncePerRequestFilter {

    private static final String PREFIXE = "Bearer ";

    /**
     * Quand un jeton bien signé est refusé, la raison est gardée dans la requête sous ce nom.
     * SecuriteErreurHandler la renvoie alors au lieu du message général, si la route était protégée.
     */
    public static final String RAISON_DU_REFUS = JwtFiltre.class.getName() + ".raisonDuRefus";

    private final JwtService jwtService;
    private final UtilisateurConnecteService utilisateurConnecteService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String entete = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (entete != null && entete.startsWith(PREFIXE)) {
            jwtService.lire(entete.substring(PREFIXE.length()))
                    .ifPresent(jeton -> authentifier(jeton, request));
        }
        chain.doFilter(request, response);
    }

    // Le compte et ses droits sont relus en base à chaque requête : un compte désactivé, un mot de passe changé
    // ou un accès à l'établissement retiré coupent l'accès sans attendre la fin du jeton.
    private void authentifier(JetonLu jeton, HttpServletRequest request) {
        try {
            CompteAuthentifie compte = utilisateurConnecteService.chargerPourJeton(jeton);
            SecurityContextHolder.getContext().setAuthentication(
                    UsernamePasswordAuthenticationToken.authenticated(compte, null, compte.getAuthorities()));
        } catch (ApplicationException refus) {
            // On continue sans identité : une route publique reste accessible, une route protégée répondra 401.
            request.setAttribute(RAISON_DU_REFUS, refus);
        }
    }
}
