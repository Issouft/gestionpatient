import { Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink, RouterOutlet } from '@angular/router';

import { SessionService } from './core/auth/session.service';

/**
 * La coquille de l'application : la barre du haut (commune à tous les écrans) et la zone où s'affiche la page.
 * Le menu du compte n'apparaît que pour une personne connectée.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule, MatDividerModule],
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

  seDeconnecter(): void {
    this.session.deconnecter();
    this.router.navigateByUrl('/connexion');
  }
}
