import { Routes } from '@angular/router';

import { choixEtablissementGuard, connecteGuard, inviteGuard } from './core/auth/gardes';

/**
 * Les pages de l'application. Chaque page est chargée seulement quand on l'ouvre (loadComponent) :
 * le premier chargement reste léger, même quand l'application grandit.
 */
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'accueil' },
  {
    path: 'connexion',
    title: 'Connexion · GestionPatient',
    canActivate: [inviteGuard],
    loadComponent: () => import('./features/auth/connexion/connexion').then((m) => m.Connexion),
  },
  {
    path: 'choix-etablissement',
    title: 'Choisir l\'établissement · GestionPatient',
    canActivate: [choixEtablissementGuard],
    loadComponent: () =>
      import('./features/auth/choix-etablissement/choix-etablissement').then((m) => m.ChoixEtablissement),
  },
  {
    path: 'accueil',
    title: 'Accueil · GestionPatient',
    canActivate: [connecteGuard],
    loadComponent: () => import('./features/accueil/accueil').then((m) => m.Accueil),
  },
  {
    path: 'profil',
    title: 'Mon profil · GestionPatient',
    canActivate: [connecteGuard],
    loadComponent: () => import('./features/compte/mon-profil/mon-profil').then((m) => m.MonProfil),
  },
  { path: '**', redirectTo: 'accueil' },
];
