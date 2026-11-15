import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Profil } from '../../core/modeles/compte';
import { Personnel, statutDe } from './personnel';

const URL = `${environment.apiUrl}/personnel`;
const membre = (id: number, actif: boolean, statut: 'ACTIVE' | 'SUSPENDUE'): Profil => ({
  id, nom: 'Fall', prenom: `P${id}`, email: `p${id}@test.sn`, role: 'MEDECIN', actif, statutDansEtablissement: statut,
});

describe('Personnel', () => {
  let page: Personnel;
  let backend: HttpTestingController;
  let confirmer: boolean;

  beforeEach(() => {
    confirmer = true;
    TestBed.configureTestingModule({
      imports: [Personnel],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        // La boîte de confirmation répond ce que le test a décidé.
        { provide: MatDialog, useValue: { open: () => ({ afterClosed: () => of(confirmer) }) } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    page = TestBed.createComponent(Personnel).componentInstance;
    backend.expectOne(URL).flush([membre(1, true, 'ACTIVE'), membre(2, false, 'ACTIVE'), membre(3, true, 'SUSPENDUE')]);
  });

  afterEach(() => backend.verify());

  it('donne à chacun son statut dans l\'établissement', () => {
    expect(page.membres().map(statutDe)).toEqual(['actif', 'attente', 'suspendu']);
    expect(page.compteurs()).toEqual({ total: 3, actifs: 1, attente: 1, suspendus: 1 });
  });

  it('filtre par recherche', () => {
    page.recherche.set('p3@');
    expect(page.membresAffiches().map((m) => m.id)).toEqual([3]);
  });

  it('suspend seulement après confirmation, puis recharge la liste', () => {
    page.suspendre(page.membres()[0]);
    backend.expectOne(`${URL}/1/desactivation`).flush(null, { status: 204, statusText: 'No Content' });
    backend.expectOne(URL).flush([]);
  });

  it('ne suspend pas si l\'administrateur annule', () => {
    confirmer = false;
    page.suspendre(page.membres()[0]);
    backend.expectNone(`${URL}/1/desactivation`);
  });

  it('renvoie l\'invitation d\'une personne en attente', () => {
    page.renvoyerInvitation(page.membres()[1]);
    backend.expectOne(`${URL}/2/invitation`).flush(null, { status: 204, statusText: 'No Content' });
    backend.expectOne(URL).flush([]);
  });
});
