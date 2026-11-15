import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';

import { environment } from '../../../../environments/environment';
import { ChangementMotDePasseDialogue } from './changement-mot-de-passe';

const URL = `${environment.apiUrl}/utilisateurs/moi/mot-de-passe`;

describe('ChangementMotDePasseDialogue', () => {
  let dialogue: ChangementMotDePasseDialogue;
  let backend: HttpTestingController;
  const fermer = vi.fn();

  beforeEach(() => {
    fermer.mockReset();
    TestBed.configureTestingModule({
      imports: [ChangementMotDePasseDialogue],
      providers: [provideHttpClient(), provideHttpClientTesting(), { provide: MatDialogRef, useValue: { close: fermer } }],
    });
    backend = TestBed.inject(HttpTestingController);
    dialogue = TestBed.createComponent(ChangementMotDePasseDialogue).componentInstance;
    dialogue.formulaire.setValue({ ancienMotDePasse: 'ancien123', nouveauMotDePasse: 'nouveau123', confirmation: 'nouveau123' });
  });

  afterEach(() => backend.verify());

  it('envoie l\'ancien et le nouveau mot de passe, jamais la confirmation', () => {
    dialogue.changer();
    const requete = backend.expectOne(URL);
    expect(requete.request.method).toBe('PUT');
    expect(requete.request.body).toEqual({ ancienMotDePasse: 'ancien123', nouveauMotDePasse: 'nouveau123' });
    requete.flush(null, { status: 204, statusText: 'No Content' });
    expect(fermer).toHaveBeenCalledWith(true);
  });

  it('affiche un mot de passe actuel faux sous son champ', () => {
    dialogue.changer();
    backend.expectOne(URL).flush({ code: 'MOT_DE_PASSE_INCORRECT' }, { status: 400, statusText: 'Bad Request' });
    expect(dialogue.formulaire.controls.ancienMotDePasse.hasError('serveur')).toBe(true);
    expect(fermer).not.toHaveBeenCalled();
  });
});
