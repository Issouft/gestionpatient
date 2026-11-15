import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { InvitationPersonnel, PersonnelService } from '../../../core/api/personnel.service';
import { codeErreur, messageErreur } from '../../../core/erreurs/message-erreur';
import { appliquerErreursServeur } from '../../../core/formulaires/validateurs';
import { LIBELLES_ROLES, Profil, ROLES_PERSONNEL, RolePersonnel } from '../../../core/modeles/compte';

/**
 * Boîte de dialogue : l'administrateur invite un membre du personnel (POST /personnel).
 * La personne reçoit un email pour choisir son mot de passe. Se ferme en renvoyant le compte créé.
 */
@Component({
  selector: 'app-invitation-personnel',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './invitation-personnel.html',
})
export class InvitationPersonnelDialogue {
  private readonly personnel = inject(PersonnelService);
  private readonly dialogue = inject(MatDialogRef<InvitationPersonnelDialogue, Profil>);

  readonly roles = ROLES_PERSONNEL.map((role) => ({ valeur: role, libelle: LIBELLES_ROLES[role] }));
  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);

  readonly formulaire = inject(NonNullableFormBuilder).group({
    role: ['MEDECIN' as RolePersonnel, Validators.required],
    prenom: ['', [Validators.required, Validators.maxLength(255)]],
    nom: ['', [Validators.required, Validators.maxLength(255)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    specialite: ['', [Validators.required, Validators.maxLength(255)]],
    numeroOrdre: ['', [Validators.required, Validators.maxLength(255)]],
  });

  readonly estMedecin = signal(true);

  constructor() {
    // Spécialité et numéro d'ordre : obligatoires pour un médecin, absents pour les autres rôles.
    this.formulaire.controls.role.valueChanges.pipe(takeUntilDestroyed()).subscribe((role) => {
      const medecin = role === 'MEDECIN';
      this.estMedecin.set(medecin);
      for (const champ of [this.formulaire.controls.specialite, this.formulaire.controls.numeroOrdre]) {
        if (medecin) {
          champ.enable();
        } else {
          champ.disable();
        }
      }
    });
  }

  inviter(): void {
    if (this.formulaire.invalid || this.enCours()) {
      this.formulaire.markAllAsTouched();
      return;
    }
    this.enCours.set(true);
    this.erreur.set(null);
    // value (et non getRawValue) : sans les champs désactivés du médecin.
    const valeurs = this.formulaire.value;
    const invitation: InvitationPersonnel = {
      role: valeurs.role!,
      prenom: valeurs.prenom!.trim(),
      nom: valeurs.nom!.trim(),
      email: valeurs.email!.trim(),
      specialite: valeurs.specialite?.trim(),
      numeroOrdre: valeurs.numeroOrdre?.trim(),
    };

    this.personnel.inviter(invitation).subscribe({
      next: (compte) => this.dialogue.close(compte),
      error: (erreur) => {
        this.enCours.set(false);
        const champs = this.formulaire.controls;
        if (codeErreur(erreur) === 'EMAIL_DEJA_UTILISE') {
          champs.email.setErrors({ serveur: 'Cet email a déjà un compte sur la plateforme.' });
        } else if (codeErreur(erreur) === 'NUMERO_ORDRE_DEJA_UTILISE') {
          champs.numeroOrdre.setErrors({ serveur: 'Un autre médecin a déjà ce numéro d\'ordre.' });
        } else if (!appliquerErreursServeur(this.formulaire, erreur)) {
          this.erreur.set(messageErreur(erreur));
        }
      },
    });
  }
}
