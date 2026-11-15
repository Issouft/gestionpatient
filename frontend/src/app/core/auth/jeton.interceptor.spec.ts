import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { jetonInterceptor } from './jeton.interceptor';
import { SessionService } from './session.service';

const API = environment.apiUrl;

describe('jetonInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let session: SessionService;
  let router: Router;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([jetonInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    session = TestBed.inject(SessionService);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    // Une session ouverte, comme après une connexion.
    session.connecter('a@test.sn', 'secret123').subscribe();
    backend.expectOne(`${API}/auth/connexion`).flush({ jeton: 'mon-jeton', etablissementId: null, etablissements: [] });
    backend.expectOne(`${API}/utilisateurs/moi`).flush({ id: 2, role: 'PATIENT' });
  });

  afterEach(() => backend.verify());

  it('ajoute le jeton aux requêtes vers notre API', () => {
    http.get(`${API}/utilisateurs/moi`).subscribe();
    const requete = backend.expectOne(`${API}/utilisateurs/moi`);
    expect(requete.request.headers.get('Authorization')).toBe('Bearer mon-jeton');
    requete.flush({});
  });

  it('n\'envoie jamais le jeton à un autre site', () => {
    http.get('https://autre-site.example/donnees').subscribe();
    const requete = backend.expectOne('https://autre-site.example/donnees');
    expect(requete.request.headers.has('Authorization')).toBe(false);
    requete.flush({});
  });

  it('ferme la session et renvoie à la connexion sur un 401', () => {
    http.get(`${API}/personnel`).subscribe({ error: () => undefined });
    backend.expectOne(`${API}/personnel`).flush(
      { code: 'NON_AUTHENTIFIE' },
      { status: 401, statusText: 'Unauthorized' },
    );

    expect(session.jeton()).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/connexion'], { queryParams: { raison: 'session-terminee' } });
  });

  it('garde la session sur un 403 : la personne est connectée, mais n\'a pas ce droit', () => {
    http.get(`${API}/etablissements`).subscribe({ error: () => undefined });
    backend.expectOne(`${API}/etablissements`).flush({ code: 'ACCES_REFUSE' }, { status: 403, statusText: 'Forbidden' });

    expect(session.jeton()).toBe('mon-jeton');
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
