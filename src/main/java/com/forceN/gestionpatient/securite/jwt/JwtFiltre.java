package com.forceN.gestionpatient.securite.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String entete = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (entete != null && entete.startsWith(PREFIXE)) {
            jwtService.extraireEmail(entete.substring(PREFIXE.length())).ifPresent(this::authentifier);
        }
        chain.doFilter(request, response);
    }

    // L'utilisateur est relu en base : un compte désactivé perd son accès sans attendre la fin du jeton.
    private void authentifier(String email) {
        try {
            UserDetails utilisateur = userDetailsService.loadUserByUsername(email);
            if (utilisateur.isEnabled()) {
                SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                        utilisateur, null, utilisateur.getAuthorities()));
            }
        } catch (UsernameNotFoundException exception) {
            SecurityContextHolder.clearContext();
        }
    }
}
