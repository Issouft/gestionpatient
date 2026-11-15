import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { EtablissementsService } from '../../../core/api/etablissements.service';
import { codeErreur, messageErreur } from '../../../core/erreurs/message-erreur';
import { appliquerErreursServeur } from '../../../core/formulaires/validateurs';
import { Profil } from '../../../core/modeles/compte';
import { Etablissement } from '../../../core/modeles/etablissement';

/**
 * Boîte de dialogue : le super-administrateur invite l'administrateur d'un établissement
 * (POST /etablissements/{id}/administrateurs). La personne choisit son mot de passe via l'email reçu.
 */
@Component({
  selector: 'app-invitation-administrateur',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>Inviter l'administrateur</h2>
    <form [formGroup]="formulaire" (ngSubmit)="inviter()" novalidate>
      <mat-dialog-content class="formulaire-dialogue">
        <p class="sous-titre">
          Pour <strong>{{ etablissement.nom }}</strong>. Il gérera le personnel de cet établissement,
          et choisira lui-même son mot de passe grâce à l'email reçu.
        </p>
        @if (erreur(); as message) {
          <p class="message message-erreur" role="alert"><mat-icon>error_outline</mat-icon>{{ message }}</p>
        }
        <div class="ligne-champs">
          <mat-form-field appearance="outline">
            <mat-label>Prénom</mat-label>
            <input matInput formControlName="prenom" />
            @if (formulaire.controls.prenom.hasError('required')) { <mat-error>Le prénom est obligatoire</mat-error> }
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Nom</mat-label>
            <input matInput formControlName="nom" />
            @if (formulaire.controls.nom.hasError('required')) { <mat-error>Le nom est obligatoire</mat-error> }
          </mat-form-field>
        </div>
        <mat-form-field appearance="outline">
          <mat-label>Email</mat-label>
          <input matInput type="email" formControlName="email" />
          @if (formulaire.controls.email.hasError('required')) { <mat-error>L'email est obligatoire</mat-error> }
          @else if (formulaire.controls.email.hasError('email')) { <mat-error>Le format de l'email est invalide</mat-error> }
          @else if (formulaire.controls.email.hasError('serveur')) { <mat-error>{{ formulaire.controls.email.getError('serveur') }}</mat-error> }
        </mat-form-field>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Annuler</button>
        <button mat-flat-button type="submit" class="bouton-marque" [disabled]="enCours()">
          @if (enCours()) { <mat-progress-spinner mode="indeterminate" diameter="20" /> } @else { Envoyer l'invitation }
        </button>
      </mat-dialog-actions>
    </form>
  `,
})
export class InvitationAdministrateurDialogue {
  private readonly etablissements = inject(EtablissementsService);
  private readonly dialogue = inject(MatDialogRef<InvitationAdministrateurDialogue, Profil>);
  readonly etablissement = inject<Etablissement>(MAT_DIALOG_DATA);

  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);

  readonly formulaire = inject(NonNullableFormBuilder).group({
    prenom: ['', [Validators.required, Validators.maxLength(255)]],
    nom: ['', [Validators.required, Validators.maxLength(255)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
  });

  inviter(): void {
    if (this.formulaire.invalid || this.enCours()) {
      this.formulaire.markAllAsTouched();
      return;
    }
    this.enCours.set(true);
    this.erreur.set(null);
    const { prenom, nom, email } = this.formulaire.getRawValue();
    this.etablissements
      .inviterAdministrateur(this.etablissement.id, { prenom: prenom.trim(), nom: nom.trim(), email: email.trim() })
      .subscribe({
        next: (compte) => this.dialogue.close(compte),
        error: (erreur) => {
          this.enCours.set(false);
          if (codeErreur(erreur) === 'EMAIL_DEJA_UTILISE') {
            this.formulaire.controls.email.setErrors({ serveur: 'Cet email a déjà un compte sur la plateforme.' });
          } else if (!appliquerErreursServeur(this.formulaire, erreur)) {
            this.erreur.set(messageErreur(erreur));
          }
        },
      });
  }
}
