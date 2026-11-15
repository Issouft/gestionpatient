import { HttpErrorResponse } from '@angular/common/http';
import { DestroyRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FormControl, FormGroup } from '@angular/forms';

import { appliquerErreursServeur, aujourdhui, dateDansLePasse, lierConfirmation } from './validateurs';

describe('validateurs', () => {
  it('refuse une confirmation différente, et la revérifie quand le mot de passe change', () => {
    const motDePasse = new FormControl('secret123');
    const confirmation = new FormControl('secret12');
    lierConfirmation(motDePasse, confirmation, TestBed.inject(DestroyRef));
    confirmation.updateValueAndValidity();
    expect(confirmation.hasError('differents')).toBe(true);

    motDePasse.setValue('secret12');
    expect(confirmation.hasError('differents')).toBe(false);
  });

  it('demande une date de naissance dans le passé', () => {
    expect(dateDansLePasse(new FormControl('1990-05-12'))).toBeNull();
    expect(dateDansLePasse(new FormControl(aujourdhui()))).toEqual({ datePasseeAttendue: true });
    expect(dateDansLePasse(new FormControl('2999-01-01'))).toEqual({ datePasseeAttendue: true });
  });

  it('place chaque erreur DONNEES_INVALIDES du backend sous son champ', () => {
    const formulaire = new FormGroup({ nom: new FormControl('x'), email: new FormControl('y') });
    const erreur = new HttpErrorResponse({
      status: 400,
      error: { code: 'DONNEES_INVALIDES', erreurs: { nom: 'Le nom est obligatoire', inconnu: 'ignoré' } },
    });

    expect(appliquerErreursServeur(formulaire, erreur)).toBe(true);
    expect(formulaire.controls.nom.getError('serveur')).toBe('Le nom est obligatoire');
    expect(formulaire.controls.email.errors).toBeNull();
  });

  it('ne fait rien pour une autre erreur', () => {
    const formulaire = new FormGroup({ nom: new FormControl('x') });
    const erreur = new HttpErrorResponse({ status: 409, error: { code: 'EMAIL_DEJA_UTILISE' } });
    expect(appliquerErreursServeur(formulaire, erreur)).toBe(false);
  });
});
