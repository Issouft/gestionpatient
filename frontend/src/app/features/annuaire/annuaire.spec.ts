import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { Annuaire } from './annuaire';

const URL_ANNUAIRE = `${environment.apiUrl}/annuaire/etablissements`;
const CLINIQUE = { id: 1, nom: 'Clinique A', type: 'CLINIQUE', ville: 'Thiès', adresse: null, telephone: null, email: null };

describe('Annuaire', () => {
  let fixture: ComponentFixture<Annuaire>;
  let page: Annuaire;
  let backend: HttpTestingController;

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [Annuaire],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    backend = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Annuaire);
    page = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    backend.verify();
    vi.useRealTimers();
  });

  it('charge les établissements à l\'ouverture', () => {
    expect(page.chargement()).toBe(true);
    vi.advanceTimersByTime(300);
    backend.expectOne(URL_ANNUAIRE).flush([CLINIQUE]);
    expect(page.etablissements()).toEqual([CLINIQUE]);
  });

  it('attend la fin de la frappe, puis cherche avec les filtres', () => {
    vi.advanceTimersByTime(300);
    backend.expectOne(URL_ANNUAIRE).flush([CLINIQUE]);

    page.filtres.setValue({ ville: 'T', type: '' });
    page.filtres.setValue({ ville: 'Thi', type: '' });
    page.filtres.setValue({ ville: 'Thiès', type: 'CLINIQUE' });
    vi.advanceTimersByTime(299);
    backend.expectNone(() => true); // pas de requête pendant la frappe

    vi.advanceTimersByTime(1);
    const requete = backend.expectOne((r) => r.url === URL_ANNUAIRE);
    expect(requete.request.params.get('ville')).toBe('Thiès');
    expect(requete.request.params.get('type')).toBe('CLINIQUE');
    requete.flush([]);
    expect(page.etablissements()).toEqual([]);
  });

  it('affiche le message si l\'annuaire ne répond pas', () => {
    vi.advanceTimersByTime(300);
    backend.expectOne(URL_ANNUAIRE).error(new ProgressEvent('error'), { status: 0 });
    expect(page.erreur()).toContain('Le serveur ne répond pas');
  });
});
