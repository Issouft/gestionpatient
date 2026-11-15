import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
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
  imports: [MatCardModule, MatListModule, MatIconModule, MatProgressBarModule],
  templateUrl: './choix-etablissement.html',
})
export class ChoixEtablissement {
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  readonly etablissements = this.session.etablissements;
  readonly courant = this.session.etablissementCourant;
  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);

  roles(acces: AccesEtablissement): string {
    return acces.roles.map((role) => LIBELLES_ROLES[role]).join(', ');
  }

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
