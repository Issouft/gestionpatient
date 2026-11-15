import { DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

import { codeErreur, erreursParChamp } from '../erreurs/message-erreur';

/*
 * Règles de saisie partagées par les formulaires. Elles reprennent celles du backend (annotations des DTO Java) :
 * le formulaire prévient tout de suite, mais c'est toujours le backend qui décide.
 */

/** Mot de passe : obligatoire, 8 à 72 caractères (comme ChoixMotDePasseRequete et PatientInscriptionDto). */
export const REGLES_MOT_DE_PASSE: ValidatorFn[] = [Validators.required, Validators.minLength(8), Validators.maxLength(72)];

/** Téléphone : chiffres, espaces et « + », de 7 à 20 caractères. */
export const REGLE_TELEPHONE: ValidatorFn = Validators.pattern(/^\+?[0-9 ]{7,20}$/);

/** Date passée (date de naissance). La valeur est au format AAAA-MM-JJ. */
export const dateDansLePasse: ValidatorFn = (controle: AbstractControl): ValidationErrors | null => {
  const valeur = controle.value as string;
  return valeur && valeur >= aujourdhui() ? { datePasseeAttendue: true } : null;
};

/** Aujourd'hui au format AAAA-MM-JJ (pour le max d'un champ date). */
export function aujourdhui(): string {
  return new Date().toISOString().slice(0, 10);
}

/**
 * La confirmation doit être identique au mot de passe (erreur « differents » sur la confirmation).
 * La confirmation est revérifiée à chaque changement du mot de passe.
 */
export function lierConfirmation(
  motDePasse: AbstractControl,
  confirmation: AbstractControl,
  destroyRef: DestroyRef,
): void {
  confirmation.addValidators((controle) =>
    controle.value && controle.value !== motDePasse.value ? { differents: true } : null,
  );
  motDePasse.valueChanges
    .pipe(takeUntilDestroyed(destroyRef))
    .subscribe(() => confirmation.updateValueAndValidity({ emitEvent: false }));
}

/**
 * Erreur DONNEES_INVALIDES du backend : met chaque message sous le champ concerné (erreur « serveur »).
 * Renvoie true si au moins un champ du formulaire a reçu un message ; sinon, l'écran affiche le message général.
 */
export function appliquerErreursServeur(formulaire: FormGroup, erreur: unknown): boolean {
  if (codeErreur(erreur) !== 'DONNEES_INVALIDES') {
    return false;
  }
  let applique = false;
  for (const [champ, message] of Object.entries(erreursParChamp(erreur))) {
    const controle = formulaire.get(champ);
    if (controle) {
      controle.setErrors({ serveur: message });
      controle.markAsTouched();
      applique = true;
    }
  }
  return applique;
}
