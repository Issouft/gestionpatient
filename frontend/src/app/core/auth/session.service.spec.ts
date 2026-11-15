import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { ConnexionReponse, Profil } from '../modeles/compte';
import { SessionService } from './session.service';

const API = environment.apiUrl;

const MEDECIN: Profil = { id: 1, nom: 'Fall', prenom: 'Moussa', email: 'm@test.sn', role: 'MEDECIN', actif: true };
const PATIENT: Profil = { id: 2, nom: 'Diop', prenom: 'Awa', email: 'a@test.sn', role: 'PATIENT', actif: true };

const DEUX_CLINIQUES: ConnexionReponse = {
  jeton: 'jeton-sans-etablissement',
  etablissementId: null,
  etablissements: [
    { etablissementId: 1, etablissementNom: 'Clinique A', roles: ['MEDECIN'] },
    { etablissementId: 4, etablissementNom: 'Clinique B', roles: ['MEDECIN'] },
  ],
};

describe('SessionService', () => {
  let session: SessionService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    session = TestBed.inject(SessionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function connecter(reponse: ConnexionReponse, profil: Profil): void {
    session.connecter('m@test.sn', 'secret123').subscribe();
    const requete = http.expectOne(`${API}/auth/connexion`);
    expect(requete.request.body).toEqual({ email: 'm@test.sn', motDePasse: 'secret123' });
    requete.flush(reponse);
    http.expectOne(`${API}/utilisateurs/moi`).flush(profil);
  }

  it('connecte un patient : pas d\'établissement à choisir', () => {
    connecter({ jeton: 'jeton-patient', etablissementId: null, etablissements: [] }, PATIENT);

    expect(session.estConnecte()).toBe(true);
    expect(session.jeton()).toBe('jeton-patient');
    expect(session.profil()?.role).toBe('PATIENT');
    expect(session.doitChoisirEtablissement()).toBe(false);
    expect(session.sansEtablissement()).toBe(false);
  });

  it('demande au personnel de choisir son établissement, puis garde le nouveau jeton', () => {
    connecter(DEUX_CLINIQUES, MEDECIN);
    expect(session.doitChoisirEtablissement()).toBe(true);

    session.choisirEtablissement(4).subscribe();
    const requete = http.expectOne(`${API}/auth/etablissement`);
    expect(requete.request.body).toEqual({ etablissementId: 4 });
    requete.flush({ ...DEUX_CLINIQUES, jeton: 'jeton-clinique-b', etablissementId: 4 });

    expect(session.jeton()).toBe('jeton-clinique-b');
    expect(session.etablissementCourant()?.etablissementNom).toBe('Clinique B');
    expect(session.doitChoisirEtablissement()).toBe(false);
  });

  it('signale le personnel qui n\'a accès à aucun établissement', () => {
    connecter({ jeton: 'jeton', etablissementId: null, etablissements: [] }, MEDECIN);
    expect(session.sansEtablissement()).toBe(true);
  });

  it('ne garde rien si les identifiants sont faux', () => {
    let erreurRecue = false;
    session.connecter('m@test.sn', 'faux').subscribe({ error: () => (erreurRecue = true) });
    http.expectOne(`${API}/auth/connexion`).flush(
      { code: 'IDENTIFIANTS_INVALIDES', detail: 'Email ou mot de passe incorrect' },
      { status: 401, statusText: 'Unauthorized' },
    );

    expect(erreurRecue).toBe(true);
    expect(session.jeton()).toBeNull();
    expect(sessionStorage.length).toBe(0);
  });

  it('se déconnecte : la session disparaît aussi de l\'onglet', () => {
    connecter(DEUX_CLINIQUES, MEDECIN);
    expect(sessionStorage.length).toBe(1);

    session.deconnecter();

    expect(session.estConnecte()).toBe(false);
    expect(session.jeton()).toBeNull();
    expect(sessionStorage.length).toBe(0);
  });

  it('ne garde jamais le mot de passe dans l\'onglet', () => {
    connecter(DEUX_CLINIQUES, MEDECIN);
    expect(JSON.stringify(sessionStorage)).not.toContain('secret123');
  });
});
