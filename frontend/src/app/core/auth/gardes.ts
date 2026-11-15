import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Role } from '../modeles/compte';
import { SessionService } from './session.service';

/*
 * Les gardes décident si une page peut s'ouvrir. C'est du CONFORT : elles évitent d'afficher une page inutile.
 * La vraie protection reste le backend, qui vérifie le jeton et les permissions à chaque requête.
 */

/** Page réservée aux personnes connectées. Le personnel qui doit choisir son établissement y est envoyé d'abord. */
export const connecteGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);
  if (!session.estConnecte()) {
    return router.createUrlTree(['/connexion']);
  }
  if (session.doitChoisirEtablissement()) {
    return router.createUrlTree(['/choix-etablissement']);
  }
  return true;
};

/** Page de choix de l'établissement : seulement pour une personne connectée qui a plusieurs établissements. */
export const choixEtablissementGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);
  if (!session.estConnecte()) {
    return router.createUrlTree(['/connexion']);
  }
  return session.etablissements().length > 1 ? true : router.createUrlTree(['/accueil']);
};

/** Page pour les visiteurs (connexion) : une personne déjà connectée va à l'accueil. */
export const inviteGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  return session.estConnecte() ? inject(Router).createUrlTree(['/accueil']) : true;
};

/** Page réservée à certains rôles (à mettre APRÈS connecteGuard). Les autres retournent à l'accueil. */
export function roleGuard(...roles: Role[]): CanActivateFn {
  return () => {
    const role = inject(SessionService).profil()?.role;
    return role !== undefined && roles.includes(role) ? true : inject(Router).createUrlTree(['/accueil']);
  };
}
