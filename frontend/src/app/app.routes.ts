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
    path: 'inscription',
    title: 'Créer un compte · GestionPatient',
    canActivate: [inviteGuard],
    loadComponent: () => import('./features/auth/inscription/inscription').then((m) => m.Inscription),
  },
  {
    path: 'mot-de-passe-oublie',
    title: 'Mot de passe oublié · GestionPatient',
    canActivate: [inviteGuard],
    loadComponent: () =>
      import('./features/auth/mot-de-passe-oublie/mot-de-passe-oublie').then((m) => m.MotDePasseOublie),
  },
  // Pages des liens reçus par email (?jeton=...) : ouvertes à tous, même à une personne déjà connectée.
  // Les adresses doivent rester celles de application.url-activation et application.url-reinitialisation (backend).
  {
    path: 'activation',
    title: 'Activer mon compte · GestionPatient',
    data: { mode: 'activation' },
    loadComponent: () =>
      import('./features/auth/choix-mot-de-passe/choix-mot-de-passe').then((m) => m.ChoixMotDePasse),
  },
  {
    path: 'reinitialisation',
    title: 'Nouveau mot de passe · GestionPatient',
    data: { mode: 'reinitialisation' },
    loadComponent: () =>
      import('./features/auth/choix-mot-de-passe/choix-mot-de-passe').then((m) => m.ChoixMotDePasse),
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
