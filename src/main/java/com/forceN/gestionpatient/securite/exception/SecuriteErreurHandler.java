package com.forceN.gestionpatient.securite.exception;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import com.forceN.gestionpatient.securite.jwt.JwtFiltre;
import com.forceN.gestionpatient.securite.service.CompteAuthentifie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Les erreurs de Spring Security arrivent avant les contrôleurs. Elles sont transmises
 * à GlobalExceptionHandler pour garder le même format que les autres erreurs.
 */
@Component
public class SecuriteErreurHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver resolver;

    public SecuriteErreurHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    // 401 : pas d'identité. Si JwtFiltre a refusé un jeton bien signé, on donne sa raison (session expirée,
    // accès à l'établissement retiré...) ; sinon (pas de jeton, jeton falsifié ou expiré), le message général.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        ApplicationException erreur = request.getAttribute(JwtFiltre.RAISON_DU_REFUS) instanceof ApplicationException refus
                ? refus
                : new ApplicationException(SecuriteErreur.NON_AUTHENTIFIE,
                        "Un jeton valide est nécessaire pour accéder à cette ressource.");
        resolver.resolveException(request, response, null, erreur);
    }

    // 403 : identité connue, mais pas la permission. Le personnel qui n'a pas encore choisi son établissement
    // n'a aucune permission : on lui dit quoi faire plutôt qu'un simple refus.
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) {
        Authentication connexion = SecurityContextHolder.getContext().getAuthentication();
        ApplicationException erreur = connexion != null
                && connexion.getPrincipal() instanceof CompteAuthentifie compte
                && compte.doitChoisirUnEtablissement()
                ? new ApplicationException(SecuriteErreur.ETABLISSEMENT_NON_CHOISI,
                        "Choisissez d'abord l'établissement où vous travaillez : POST /auth/etablissement.")
                : new ApplicationException(SecuriteErreur.ACCES_REFUSE,
                        "Vous n'avez pas le droit d'accéder à cette ressource.");
        resolver.resolveException(request, response, null, erreur);
    }
}
