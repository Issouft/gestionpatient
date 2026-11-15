import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

/** Ce qu'affiche la boîte de confirmation. */
export interface DonneesConfirmation {
  titre: string;
  message: string;
  boutonConfirmer: string;
  /** Action qui retire un accès (suspendre, désactiver) : bouton rouge. */
  danger?: boolean;
}

/**
 * Boîte « Êtes-vous sûr ? » avant une action sensible. Renvoie true si la personne confirme.
 *   this.dialog.open(Confirmation, { data: {...} }).afterClosed().subscribe((oui) => ...)
 */
@Component({
  selector: 'app-confirmation',
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>{{ donnees.titre }}</h2>
    <mat-dialog-content>
      <p>{{ donnees.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false">Annuler</button>
      <button mat-flat-button [class.bouton-danger]="donnees.danger" [mat-dialog-close]="true" cdkFocusInitial>
        {{ donnees.boutonConfirmer }}
      </button>
    </mat-dialog-actions>
  `,
})
export class Confirmation {
  readonly donnees = inject<DonneesConfirmation>(MAT_DIALOG_DATA);
}
