import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

import { EtablissementsService, NouvelEtablissement } from '../../../core/api/etablissements.service';
import { messageErreur } from '../../../core/erreurs/message-erreur';
import { REGLE_TELEPHONE, appliquerErreursServeur } from '../../../core/formulaires/validateurs';
import { Etablissement, LIBELLES_TYPES, TypeEtablissement } from '../../../core/modeles/etablissement';

/** Boîte de dialogue : le super-administrateur crée un établissement (POST /etablissements). */
@Component({
  selector: 'app-creation-etablissement',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>Nouvel établissement</h2>
    <form [formGroup]="formulaire" (ngSubmit)="creer()" novalidate>
      <mat-dialog-content class="formulaire-dialogue">
        @if (erreur(); as message) {
          <p class="message message-erreur" role="alert"><mat-icon>error_outline</mat-icon>{{ message }}</p>
        }
        <div class="ligne-champs">
          <mat-form-field appearance="outline">
            <mat-label>Nom</mat-label>
            <input matInput formControlName="nom" placeholder="Clinique du Plateau" />
            @if (formulaire.controls.nom.hasError('required')) { <mat-error>Le nom est obligatoire</mat-error> }
            @else if (formulaire.controls.nom.hasError('serveur')) { <mat-error>{{ formulaire.controls.nom.getError('serveur') }}</mat-error> }
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Type</mat-label>
            <mat-select formControlName="type">
              @for (type of types; track type[0]) { <mat-option [value]="type[0]">{{ type[1] }}</mat-option> }
            </mat-select>
            @if (formulaire.controls.type.hasError('required')) { <mat-error>Le type est obligatoire</mat-error> }
          </mat-form-field>
        </div>
        <div class="ligne-champs">
          <mat-form-field appearance="outline">
            <mat-label>Adresse</mat-label>
            <input matInput formControlName="adresse" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Ville</mat-label>
            <input matInput formControlName="ville" />
          </mat-form-field>
        </div>
        <div class="ligne-champs">
          <mat-form-field appearance="outline">
            <mat-label>Téléphone</mat-label>
            <input matInput type="tel" formControlName="telephone" />
            @if (formulaire.controls.telephone.hasError('pattern')) { <mat-error>Chiffres, espaces et « + » seulement</mat-error> }
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Email de contact</mat-label>
            <input matInput type="email" formControlName="email" />
            <mat-hint>Pas un compte de connexion</mat-hint>
            @if (formulaire.controls.email.hasError('email')) { <mat-error>Le format de l'email est invalide</mat-error> }
            @else if (formulaire.controls.email.hasError('serveur')) { <mat-error>{{ formulaire.controls.email.getError('serveur') }}</mat-error> }
          </mat-form-field>
        </div>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Annuler</button>
        <button mat-flat-button type="submit" class="bouton-marque" [disabled]="enCours()">
          @if (enCours()) { <mat-progress-spinner mode="indeterminate" diameter="20" /> } @else { Créer }
        </button>
      </mat-dialog-actions>
    </form>
  `,
})
export class CreationEtablissementDialogue {
  private readonly etablissements = inject(EtablissementsService);
  private readonly dialogue = inject(MatDialogRef<CreationEtablissementDialogue, Etablissement>);

  readonly types = Object.entries(LIBELLES_TYPES) as [TypeEtablissement, string][];
  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);

  readonly formulaire = inject(NonNullableFormBuilder).group({
    nom: ['', [Validators.required, Validators.maxLength(255)]],
    type: ['CLINIQUE' as TypeEtablissement, Validators.required],
    adresse: ['', Validators.maxLength(255)],
    ville: ['', Validators.maxLength(255)],
    telephone: ['', REGLE_TELEPHONE],
    email: ['', [Validators.email, Validators.maxLength(255)]],
  });

  creer(): void {
    if (this.formulaire.invalid || this.enCours()) {
      this.formulaire.markAllAsTouched();
      return;
    }
    this.enCours.set(true);
    this.erreur.set(null);
    // Les champs vides sont envoyés tels quels : le backend les enregistre comme absents.
    const etablissement: NouvelEtablissement = this.formulaire.getRawValue();
    this.etablissements.creer(etablissement).subscribe({
      next: (cree) => this.dialogue.close(cree),
      error: (erreur) => {
        this.enCours.set(false);
        if (!appliquerErreursServeur(this.formulaire, erreur)) {
          this.erreur.set(messageErreur(erreur));
        }
      },
    });
  }
}
