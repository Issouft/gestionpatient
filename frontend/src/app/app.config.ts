import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import localeFr from '@angular/common/locales/fr';
import {
  ApplicationConfig,
  LOCALE_ID,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { routes } from './app.routes';
import { jetonInterceptor } from './core/auth/jeton.interceptor';
import { SessionService } from './core/auth/session.service';

// Dates et nombres affichés en français (ex. « 12 mai 1990 »).
registerLocaleData(localeFr);

/** La configuration de l'application : ce qui est disponible partout, une seule fois. */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    { provide: LOCALE_ID, useValue: 'fr' },
    // withComponentInputBinding : un paramètre d'adresse (?raison=...) arrive directement dans un input() du composant.
    provideRouter(routes, withComponentInputBinding()),
    // Toutes les requêtes passent par l'intercepteur, qui ajoute le jeton et gère les 401.
    provideHttpClient(withInterceptors([jetonInterceptor])),
    // Avant le premier écran : si un jeton est resté dans l'onglet, on recharge le profil.
    provideAppInitializer(() => inject(SessionService).restaurer()),
  ],
};
