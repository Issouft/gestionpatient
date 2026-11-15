import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';

import { environment } from '../../../../environments/environment';
import { InvitationPersonnelDialogue } from './invitation-personnel';

const URL = `${environment.apiUrl}/personnel`;

describe('InvitationPersonnelDialogue', () => {
  let dialogue: InvitationPersonnelDialogue;
  let backend: HttpTestingController;
  const fermer = vi.fn();

  beforeEach(() => {
    fermer.mockReset();
    TestBed.configureTestingModule({
      imports: [InvitationPersonnelDialogue],
      providers: [provideHttpClient(), provideHttpClientTesting(), { provide: MatDialogRef, useValue: { close: fermer } }],
    });
    backend = TestBed.inject(HttpTestingController);
    dialogue = TestBed.createComponent(InvitationPersonnelDialogue).componentInstance;
  });

  afterEach(() => backend.verify());

  it('exige spécialité et numéro d\'ordre pour un médecin seulement', () => {
    dialogue.formulaire.patchValue({ prenom: 'Awa', nom: 'Diop', email: 'awa@clinique.sn' });
    expect(dialogue.formulaire.valid).toBe(false); // médecin par défaut, sans spécialité

    dialogue.formulaire.controls.role.setValue('SECRETAIRE');
    expect(dialogue.estMedecin()).toBe(false);
    expect(dialogue.formulaire.valid).toBe(true);

    dialogue.inviter();
    const requete = backend.expectOne(URL);
    expect(requete.request.body).toEqual({
      role: 'SECRETAIRE', prenom: 'Awa', nom: 'Diop', email: 'awa@clinique.sn', specialite: undefined, numeroOrdre: undefined,
    });
    requete.flush({ id: 9, email: 'awa@clinique.sn' });
    expect(fermer).toHaveBeenCalledWith({ id: 9, email: 'awa@clinique.sn' });
  });

  it('affiche un numéro d\'ordre déjà pris sous son champ', () => {
    dialogue.formulaire.patchValue({
      prenom: 'Ali', nom: 'Sarr', email: 'ali@clinique.sn', specialite: 'Cardiologie', numeroOrdre: 'ORD-1',
    });
    dialogue.inviter();
    backend.expectOne(URL).flush({ code: 'NUMERO_ORDRE_DEJA_UTILISE' }, { status: 409, statusText: 'Conflict' });

    expect(dialogue.formulaire.controls.numeroOrdre.hasError('serveur')).toBe(true);
    expect(fermer).not.toHaveBeenCalled();
  });
});
