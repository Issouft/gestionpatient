import { Component, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';

import { SessionService } from '../../../core/auth/session.service';
import { messageErreur } from '../../../core/erreurs/message-erreur';
import { AccesEtablissement, LIBELLES_ROLES } from '../../../core/modeles/compte';

/**
 * Le personnel qui travaille dans plusieurs établissements choisit celui de la session (POST /auth/etablissement).
 * Sert aussi à changer d'établissement plus tard (menu du compte).
 */
@Component({
  selector: 'app-choix-etablissement',
  imports: [MatIconModule],
  templateUrl: './choix-etablissement.html',
  styles: `
    .roles { display: flex; flex-wrap: wrap; gap: 6px; }
    .actuel { border-color: var(--mat-sys-primary); background: var(--mat-sys-primary-container); }
  `,
})
export class ChoixEtablissement {
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  readonly libelles = LIBELLES_ROLES;
  readonly etablissements = this.session.etablissements;
  readonly courant = this.session.etablissementCourant;
  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);

  choisir(acces: AccesEtablissement): void {
    if (this.enCours()) {
      return;
    }
    this.enCours.set(true);
    this.erreur.set(null);
    this.session.choisirEtablissement(acces.etablissementId).subscribe({
      next: () => this.router.navigateByUrl('/accueil'),
      error: (erreur) => {
        // Ex. 403 ETABLISSEMENT_NON_AUTORISE : l'accès a été retiré entre la connexion et le choix.
        this.erreur.set(messageErreur(erreur));
        this.enCours.set(false);
      },
    });
  }
}
