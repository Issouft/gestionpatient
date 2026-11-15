import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { ModificationProfil, MonCompteService } from '../../../core/api/mon-compte.service';
import { SessionService } from '../../../core/auth/session.service';
import { messageErreur } from '../../../core/erreurs/message-erreur';
import { REGLE_TELEPHONE, appliquerErreursServeur } from '../../../core/formulaires/validateurs';
import { Profil } from '../../../core/modeles/compte';

/**
 * Boîte de dialogue : modifier son profil (PUT /utilisateurs/moi).
 * Nom et prénom pour tous ; téléphone et adresse seulement pour un patient. L'email ne change pas ici.
 */
@Component({
  selector: 'app-modification-profil',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <h2 mat-dialog-title>Modifier mes informations</h2>
    <form [formGroup]="formulaire" (ngSubmit)="enregistrer()" novalidate>
      <mat-dialog-content class="formulaire-dialogue">
        @if (erreur(); as message) {
          <p class="message message-erreur" role="alert"><mat-icon>error_outline</mat-icon>{{ message }}</p>
        }
        <div class="ligne-champs">
          <mat-form-field appearance="outline">
            <mat-label>Prénom</mat-label>
            <input matInput formControlName="prenom" autocomplete="given-name" />
            @if (formulaire.controls.prenom.hasError('required')) { <mat-error>Le prénom est obligatoire</mat-error> }
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Nom</mat-label>
            <input matInput formControlName="nom" autocomplete="family-name" />
            @if (formulaire.controls.nom.hasError('required')) { <mat-error>Le nom est obligatoire</mat-error> }
          </mat-form-field>
        </div>
        @if (estPatient) {
          <div class="ligne-champs">
            <mat-form-field appearance="outline">
              <mat-label>Téléphone</mat-label>
              <input matInput type="tel" formControlName="telephone" autocomplete="tel" />
              @if (formulaire.controls.telephone.hasError('required')) { <mat-error>Le téléphone est obligatoire</mat-error> }
              @else if (formulaire.controls.telephone.hasError('pattern')) { <mat-error>Chiffres, espaces et « + » seulement</mat-error> }
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Adresse</mat-label>
              <input matInput formControlName="adresse" autocomplete="street-address" />
              @if (formulaire.controls.adresse.hasError('required')) { <mat-error>L'adresse est obligatoire</mat-error> }
            </mat-form-field>
          </div>
        }
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Annuler</button>
        <button mat-flat-button type="submit" class="bouton-marque" [disabled]="enCours()">
          @if (enCours()) { <mat-progress-spinner mode="indeterminate" diameter="20" /> } @else { Enregistrer }
        </button>
      </mat-dialog-actions>
    </form>
  `,
})
export class ModificationProfilDialogue {
  private readonly compte = inject(MonCompteService);
  private readonly session = inject(SessionService);
  private readonly dialogue = inject(MatDialogRef<ModificationProfilDialogue, Profil>);

  private readonly profil = this.session.profil()!;
  readonly estPatient = this.profil.role === 'PATIENT';
  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);

  readonly formulaire = inject(NonNullableFormBuilder).group({
    prenom: [this.profil.prenom, [Validators.required, Validators.maxLength(255)]],
    nom: [this.profil.nom, [Validators.required, Validators.maxLength(255)]],
    telephone: [{ value: this.profil.telephone ?? '', disabled: !this.estPatient }, [Validators.required, REGLE_TELEPHONE]],
    adresse: [{ value: this.profil.adresse ?? '', disabled: !this.estPatient }, [Validators.required, Validators.maxLength(255)]],
  });

  enregistrer(): void {
    if (this.formulaire.invalid || this.enCours()) {
      this.formulaire.markAllAsTouched();
      return;
    }
    this.enCours.set(true);
    this.erreur.set(null);
    const valeurs = this.formulaire.value; // sans téléphone ni adresse pour le personnel (champs désactivés)
    const modification: ModificationProfil = {
      prenom: valeurs.prenom!.trim(),
      nom: valeurs.nom!.trim(),
      telephone: valeurs.telephone?.trim(),
      adresse: valeurs.adresse?.trim(),
    };
    this.compte.modifierProfil(modification).subscribe({
      next: (profil) => {
        this.session.mettreAJourProfil(profil);
        this.dialogue.close(profil);
      },
      error: (erreur) => {
        this.enCours.set(false);
        if (!appliquerErreursServeur(this.formulaire, erreur)) {
          this.erreur.set(messageErreur(erreur));
        }
      },
    });
  }
}
