import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { ConnexionReponse, Profil } from '../modeles/compte';
import { connecteGuard, inviteGuard, roleGuard } from './gardes';
import { SessionService } from './session.service';

const API = environment.apiUrl;

describe('gardes', () => {
  let backend: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()] });
    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  function connecter(reponse: ConnexionReponse, profil: Partial<Profil>): void {
    TestBed.inject(SessionService).connecter('x@test.sn', 'secret123').subscribe();
    backend.expectOne(`${API}/auth/connexion`).flush(reponse);
    backend.expectOne(`${API}/utilisateurs/moi`).flush(profil);
  }

  /** Lance une garde comme le ferait le routeur, et renvoie l'adresse de redirection (ou true). */
  function lancer(garde: typeof connecteGuard): true | string {
    const resultat = TestBed.runInInjectionContext(() =>
      garde({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
    return resultat instanceof UrlTree ? router.serializeUrl(resultat) : (resultat as true);
  }

  it('renvoie un visiteur à la connexion', () => {
    expect(lancer(connecteGuard)).toBe('/connexion');
    expect(lancer(inviteGuard)).toBe(true);
  });

  it('laisse passer une personne connectée, et l\'éloigne de la page de connexion', () => {
    connecter({ jeton: 'j', etablissementId: null, etablissements: [] }, { role: 'PATIENT' });
    expect(lancer(connecteGuard)).toBe(true);
    expect(lancer(inviteGuard)).toBe('/accueil');
  });

  it('envoie le personnel à plusieurs établissements choisir le sien d\'abord', () => {
    connecter(
      {
        jeton: 'j',
        etablissementId: null,
        etablissements: [
          { etablissementId: 1, etablissementNom: 'A', roles: ['MEDECIN'] },
          { etablissementId: 2, etablissementNom: 'B', roles: ['MEDECIN'] },
        ],
      },
      { role: 'MEDECIN' },
    );
    expect(lancer(connecteGuard)).toBe('/choix-etablissement');
  });

  it('réserve une page aux rôles indiqués', () => {
    connecter({ jeton: 'j', etablissementId: null, etablissements: [] }, { role: 'PATIENT' });
    expect(lancer(roleGuard('PATIENT'))).toBe(true);
    expect(lancer(roleGuard('ADMINISTRATEUR'))).toBe('/accueil');
  });
});
