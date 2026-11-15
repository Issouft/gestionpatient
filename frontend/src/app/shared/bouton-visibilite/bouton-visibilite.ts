import { Component, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

/**
 * Bouton « œil » qui affiche ou masque un mot de passe. À placer en suffixe du champ :
 *   <input matInput #mdp type="password" ... />
 *   <app-bouton-visibilite matSuffix [champ]="mdp" />
 */
@Component({
  selector: 'app-bouton-visibilite',
  imports: [MatButtonModule, MatIconModule],
  template: `
    <button
      mat-icon-button
      type="button"
      (click)="basculer()"
      [attr.aria-label]="visible() ? 'Masquer le mot de passe' : 'Afficher le mot de passe'"
      [attr.aria-pressed]="visible()"
    >
      <mat-icon>{{ visible() ? 'visibility_off' : 'visibility' }}</mat-icon>
    </button>
  `,
})
export class BoutonVisibilite {
  readonly champ = input.required<HTMLInputElement>();
  readonly visible = signal(false);

  basculer(): void {
    this.visible.update((visible) => !visible);
    this.champ().type = this.visible() ? 'text' : 'password';
  }
}
