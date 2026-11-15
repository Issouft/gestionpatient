import { Component, DestroyRef, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { MonCompteService } from '../../../core/api/mon-compte.service';
import { codeErreur, messageErreur } from '../../../core/erreurs/message-erreur';
import { REGLES_MOT_DE_PASSE, appliquerErreursServeur, lierConfirmation } from '../../../core/formulaires/validateurs';
import { BoutonVisibilite } from '../../../shared/bouton-visibilite/bouton-visibilite';

/**
 * Boîte de dialogue : changer son mot de passe (PUT /utilisateurs/moi/mot-de-passe).
 * Se ferme avec true si c'est fait : le backend a fermé TOUTES les sessions, il faut se reconnecter.
 */
@Component({
  selector: 'app-changement-mot-de-passe',
  imports: [ReactiveFormsModule, BoutonVisibilite, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <h2 mat-dialog-title>Changer mon mot de passe</h2>
    <form [formGroup]="formulaire" (ngSubmit)="changer()" novalidate>
      <mat-dialog-content class="formulaire-dialogue">
        <p class="sous-titre">Toutes vos sessions seront fermées : vous vous reconnecterez avec le nouveau mot de passe.</p>
        @if (erreur(); as message) {
          <p class="message message-erreur" role="alert"><mat-icon>error_outline</mat-icon>{{ message }}</p>
        }
        <mat-form-field appearance="outline">
          <mat-label>Mot de passe actuel</mat-label>
          <input matInput #actuel type="password" formControlName="ancienMotDePasse" autocomplete="current-password" />
          <app-bouton-visibilite matSuffix [champ]="actuel" />
          @if (formulaire.controls.ancienMotDePasse.hasError('required')) { <mat-error>Le mot de passe actuel est obligatoire</mat-error> }
          @else if (formulaire.controls.ancienMotDePasse.hasError('serveur')) { <mat-error>{{ formulaire.controls.ancienMotDePasse.getError('serveur') }}</mat-error> }
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Nouveau mot de passe</mat-label>
          <input matInput #nouveau type="password" formControlName="nouveauMotDePasse" autocomplete="new-password" />
          <app-bouton-visibilite matSuffix [champ]="nouveau" />
          <mat-hint>8 caractères minimum</mat-hint>
          @if (formulaire.controls.nouveauMotDePasse.hasError('required')) { <mat-error>Le nouveau mot de passe est obligatoire</mat-error> }
          @else if (formulaire.controls.nouveauMotDePasse.hasError('minlength') || formulaire.controls.nouveauMotDePasse.hasError('maxlength')) { <mat-error>Entre 8 et 72 caractères</mat-error> }
          @else if (formulaire.controls.nouveauMotDePasse.hasError('serveur')) { <mat-error>{{ formulaire.controls.nouveauMotDePasse.getError('serveur') }}</mat-error> }
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Confirmation</mat-label>
          <input matInput #confirmation type="password" formControlName="confirmation" autocomplete="new-password" />
          <app-bouton-visibilite matSuffix [champ]="confirmation" />
          @if (formulaire.controls.confirmation.hasError('required')) { <mat-error>Confirmez le nouveau mot de passe</mat-error> }
          @else if (formulaire.controls.confirmation.hasError('differents')) { <mat-error>Les deux mots de passe sont différents</mat-error> }
        </mat-form-field>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Annuler</button>
        <button mat-flat-button type="submit" class="bouton-marque" [disabled]="enCours()">
          @if (enCours()) { <mat-progress-spinner mode="indeterminate" diameter="20" /> } @else { Changer }
        </button>
      </mat-dialog-actions>
    </form>
  `,
})
export class ChangementMotDePasseDialogue {
  private readonly compte = inject(MonCompteService);
  private readonly dialogue = inject(MatDialogRef<ChangementMotDePasseDialogue, boolean>);

  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);

  readonly formulaire = inject(NonNullableFormBuilder).group({
    ancienMotDePasse: ['', Validators.required],
    nouveauMotDePasse: ['', REGLES_MOT_DE_PASSE],
    confirmation: ['', Validators.required],
  });

  constructor() {
    const champs = this.formulaire.controls;
    lierConfirmation(champs.nouveauMotDePasse, champs.confirmation, inject(DestroyRef));
  }

  changer(): void {
    if (this.formulaire.invalid || this.enCours()) {
      this.formulaire.markAllAsTouched();
      return;
    }
    this.enCours.set(true);
    this.erreur.set(null);
    const { ancienMotDePasse, nouveauMotDePasse } = this.formulaire.getRawValue();
    this.compte.changerMotDePasse(ancienMotDePasse, nouveauMotDePasse).subscribe({
      next: () => this.dialogue.close(true),
      error: (erreur) => {
        this.enCours.set(false);
        if (codeErreur(erreur) === 'MOT_DE_PASSE_INCORRECT') {
          this.formulaire.controls.ancienMotDePasse.setErrors({ serveur: 'Le mot de passe actuel est incorrect.' });
        } else if (!appliquerErreursServeur(this.formulaire, erreur)) {
          this.erreur.set(messageErreur(erreur));
        }
      },
    });
  }
}
