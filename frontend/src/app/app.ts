import { Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { Router, RouterLink, RouterOutlet } from '@angular/router';

import { SessionService } from './core/auth/session.service';
import { LIBELLES_ROLES } from './core/modeles/compte';

/**
 * La coquille de l'application : la zone où s'affiche la page, et pour une personne connectée,
 * la barre du haut (logo, établissement, menu du compte) et le pied de page.
 * Les pages d'accès (connexion...) occupent tout l'écran : elles n'ont pas de barre.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, MatButtonModule, MatIconModule, MatMenuModule, MatDividerModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  readonly estConnecte = this.session.estConnecte;
  readonly profil = this.session.profil;
  readonly etablissement = this.session.etablissementCourant;
  readonly plusieursEtablissements = computed(() => this.session.etablissements().length > 1);

  readonly libelleRole = computed(() => {
    const role = this.profil()?.role;
    return role ? LIBELLES_ROLES[role] : '';
  });

  /** « Awa Diop » → « AD », pour l'avatar. */
  readonly initiales = computed(() => {
    const profil = this.profil();
    return profil ? `${profil.prenom.charAt(0)}${profil.nom.charAt(0)}`.toUpperCase() : '';
  });

  seDeconnecter(): void {
    this.session.deconnecter();
    this.router.navigateByUrl('/connexion');
  }
}
