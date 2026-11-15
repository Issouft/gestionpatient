import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { SessionService } from './session.service';

// Un 401 sur la connexion veut dire « identifiants incorrects » : l'écran de connexion l'affiche lui-même.
const ROUTE_CONNEXION = `${environment.apiUrl}/auth/connexion`;

/**
 * Passe sur toutes les requêtes HTTP de l'application :
 * 1. ajoute le jeton, SEULEMENT pour notre API (jamais pour un autre site) ;
 * 2. sur un 401 (jeton expiré, mot de passe changé, accès retiré...), ferme la session et renvoie à la connexion.
 */
export const jetonInterceptor: HttpInterceptorFn = (requete, suivant) => {
  const session = inject(SessionService);
  const router = inject(Router);

  const versNotreApi = requete.url.startsWith(environment.apiUrl);
  const jeton = session.jeton();
  const requeteEnvoyee = versNotreApi && jeton
    ? requete.clone({ setHeaders: { Authorization: `Bearer ${jeton}` } })
    : requete;

  return suivant(requeteEnvoyee).pipe(
    catchError((erreur: unknown) => {
      if (versNotreApi && jeton && erreur instanceof HttpErrorResponse
          && erreur.status === 401 && requete.url !== ROUTE_CONNEXION) {
        session.deconnecter();
        router.navigate(['/connexion'], { queryParams: { raison: 'session-terminee' } });
      }
      return throwError(() => erreur);
    }),
  );
};
