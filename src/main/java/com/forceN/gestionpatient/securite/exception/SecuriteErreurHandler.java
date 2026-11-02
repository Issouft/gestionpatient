package com.forceN.gestionpatient.securite.exception;

import com.forceN.gestionpatient.commun.exception.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
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

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        resolver.resolveException(request, response, null, new ApplicationException(
                SecuriteErreur.NON_AUTHENTIFIE, "Un jeton valide est nécessaire pour accéder à cette ressource."));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) {
        resolver.resolveException(request, response, null, new ApplicationException(
                SecuriteErreur.ACCES_REFUSE, "Vous n'avez pas le droit d'accéder à cette ressource."));
    }
}
