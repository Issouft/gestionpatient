import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Etablissements } from './etablissements';

const URL = `${environment.apiUrl}/etablissements`;
const ETAB = { id: 4, nom: 'Clinique B', type: 'CLINIQUE', ville: 'Thiès', adresse: null, telephone: null, email: null, actif: true };

describe('Etablissements', () => {
  let page: Etablissements;
  let backend: HttpTestingController;
  let reponseDialogue: unknown;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Etablissements],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MatDialog, useValue: { open: () => ({ afterClosed: () => of(reponseDialogue) }) } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    page = TestBed.createComponent(Etablissements).componentInstance;
    backend.expectOne(URL).flush([ETAB, { ...ETAB, id: 5, nom: 'Cabinet C', actif: false }]);
  });

  afterEach(() => backend.verify());

  it('compte les établissements actifs et filtre par nom ou ville', () => {
    expect(page.nombreActifs()).toBe(1);
    page.recherche.set('cabinet');
    expect(page.affiches().map((e) => e.id)).toEqual([5]);
  });

  it('désactive après confirmation, puis recharge la liste', () => {
    reponseDialogue = true;
    page.desactiver(ETAB as never);
    backend.expectOne(`${URL}/4/desactivation`).flush(null, { status: 204, statusText: 'No Content' });
    backend.expectOne(URL).flush([]);
  });

  it('recharge la liste après la création d\'un établissement', () => {
    reponseDialogue = { ...ETAB, id: 6, nom: 'Nouvelle clinique' };
    page.creer();
    backend.expectOne(URL).flush([]);
  });
});
