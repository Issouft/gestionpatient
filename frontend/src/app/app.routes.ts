import { Routes } from '@angular/router';

import { choixEtablissementGuard, connecteGuard, inviteGuard, roleGuard } from './core/auth/gardes';

/**
 * Les pages de l'application. Chaque page est chargée seulement quand on l'ouvre (loadComponent) :
 * le premier chargement reste léger, même quand l'application grandit.
 * data.pleinEcran : page d'accès (connexion...) affichée sans la barre du haut (voir app.ts).
 */
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'accueil' },
  {
    path: 'connexion',
    data: { pleinEcran: true },
    title: 'Connexion · GestionPatient',
    canActivate: [inviteGuard],
    loadComponent: () => import('./features/auth/connexion/connexion').then((m) => m.Connexion),
  },
  {
    path: 'inscription',
    data: { pleinEcran: true },
    title: 'Créer un compte · GestionPatient',
    canActivate: [inviteGuard],
    loadComponent: () => import('./features/auth/inscription/inscription').then((m) => m.Inscription),
  },
  {
    path: 'mot-de-passe-oublie',
    data: { pleinEcran: true },
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
    data: { mode: 'activation', pleinEcran: true },
    loadComponent: () =>
      import('./features/auth/choix-mot-de-passe/choix-mot-de-passe').then((m) => m.ChoixMotDePasse),
  },
  {
    path: 'reinitialisation',
    title: 'Nouveau mot de passe · GestionPatient',
    data: { mode: 'reinitialisation', pleinEcran: true },
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
  // Annuaire public : ouvert aux visiteurs, aux patients et au personnel.
  {
    path: 'annuaire',
    title: 'Trouver une clinique · GestionPatient',
    loadComponent: () => import('./features/annuaire/annuaire').then((m) => m.Annuaire),
  },
  {
    path: 'annuaire/:id',
    title: 'Clinique · GestionPatient',
    loadComponent: () =>
      import('./features/annuaire/fiche-etablissement/fiche-etablissement').then((m) => m.FicheEtablissement),
  },
  {
    path: 'mes-cliniques',
    title: 'Mes cliniques · GestionPatient',
    canActivate: [connecteGuard, roleGuard('PATIENT')],
    loadComponent: () => import('./features/patient/mes-cliniques/mes-cliniques').then((m) => m.MesCliniques),
  },
  {
    path: 'personnel',
    title: 'Personnel · GestionPatient',
    canActivate: [connecteGuard, roleGuard('ADMINISTRATEUR')],
    loadComponent: () => import('./features/personnel/personnel').then((m) => m.Personnel),
  },
  {
    path: 'etablissements',
    title: 'Établissements · GestionPatient',
    canActivate: [connecteGuard, roleGuard('SUPER_ADMINISTRATEUR')],
    loadComponent: () => import('./features/etablissements/etablissements').then((m) => m.Etablissements),
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
