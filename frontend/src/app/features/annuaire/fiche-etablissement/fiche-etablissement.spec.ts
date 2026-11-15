import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../../environments/environment';
import { SessionService } from '../../../core/auth/session.service';
import { FicheEtablissement } from './fiche-etablissement';

const API = environment.apiUrl;
const CLINIQUE = { id: 7, nom: 'Clinique du Plateau', type: 'CLINIQUE', ville: 'Dakar', adresse: null, telephone: null, email: null };
const MEDECINS = [
  { id: 1, nom: 'Ndiaye', prenom: 'Fatou', specialite: 'Cardiologie' },
  { id: 2, nom: 'Sarr', prenom: 'Ali', specialite: 'Pédiatrie' },
];

describe('FicheEtablissement', () => {
  let fixture: ComponentFixture<FicheEtablissement>;
  let page: FicheEtablissement;
  let backend: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [FicheEtablissement],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  function connecterPatient(): void {
    TestBed.inject(SessionService).connecter('a@test.sn', 'secret123').subscribe();
    backend.expectOne(`${API}/auth/connexion`).flush({ jeton: 'j', etablissementId: null, etablissements: [] });
    backend.expectOne(`${API}/utilisateurs/moi`).flush({ id: 3, role: 'PATIENT', prenom: 'Awa', nom: 'Diop' });
  }

  async function ouvrir(mesCliniques?: unknown[]): Promise<void> {
    fixture = TestBed.createComponent(FicheEtablissement);
    page = fixture.componentInstance;
    fixture.componentRef.setInput('id', '7');
    fixture.detectChanges();
    await fixture.whenStable();
    backend.expectOne(`${API}/annuaire/etablissements/7`).flush(CLINIQUE);
    backend.expectOne(`${API}/annuaire/etablissements/7/medecins`).flush(MEDECINS);
    if (mesCliniques) {
      backend.expectOne(`${API}/patients/moi/etablissements`).flush(mesCliniques);
    }
    fixture.detectChanges();
  }

  it('propose au visiteur de créer son compte dans cette clinique', async () => {
    await ouvrir();
    expect(page.estVisiteur()).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Créer mon compte ici');
    expect(page.specialites()).toEqual(['Cardiologie', 'Pédiatrie']);
  });

  it('filtre les médecins par spécialité', async () => {
    await ouvrir();
    page.specialiteChoisie.set('Pédiatrie');
    expect(page.medecinsAffiches().map((m) => m.nom)).toEqual(['Sarr']);
  });

  it('permet au patient de rejoindre la clinique', async () => {
    connecterPatient();
    await ouvrir([]);
    expect(page.rejointe()).toBe(false);

    page.rejoindre();
    backend.expectOne((r) => r.method === 'POST' && r.url === `${API}/patients/moi/etablissements/7`)
      .flush(null, { status: 204, statusText: 'No Content' });

    expect(page.rejointe()).toBe(true);
  });

  it('montre au patient qu\'il est déjà suivi', async () => {
    connecterPatient();
    await ouvrir([CLINIQUE]);
    expect(page.rejointe()).toBe(true);
  });

  it('affiche le message du backend si la clinique a suspendu l\'adhésion', async () => {
    connecterPatient();
    await ouvrir([]);
    page.rejoindre();
    backend.expectOne(`${API}/patients/moi/etablissements/7`).flush(
      { code: 'ADHESION_SUSPENDUE', detail: 'Cet établissement a suspendu votre adhésion. Contactez-le directement.' },
      { status: 403, statusText: 'Forbidden' },
    );
    expect(page.rejointe()).toBe(false);
    expect(page.erreurAdhesion()).toContain('Contactez-le');
  });

  it('annonce un établissement introuvable', async () => {
    fixture = TestBed.createComponent(FicheEtablissement);
    page = fixture.componentInstance;
    fixture.componentRef.setInput('id', '99');
    fixture.detectChanges();
    await fixture.whenStable();
    backend.expectOne(`${API}/annuaire/etablissements/99`).flush(
      { code: 'ETABLISSEMENT_INTROUVABLE' },
      { status: 404, statusText: 'Not Found' },
    );
    // forkJoin s'arrête à la première erreur : la requête des médecins est annulée.
    backend.match(`${API}/annuaire/etablissements/99/medecins`);
    expect(page.etat()).toBe('introuvable');
  });
});
