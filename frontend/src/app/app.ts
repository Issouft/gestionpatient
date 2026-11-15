import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { ActivatedRouteSnapshot, NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';

import { SessionService } from './core/auth/session.service';
import { LIBELLES_ROLES } from './core/modeles/compte';

/**
 * La coquille de l'application : la barre du haut, la zone où s'affiche la page et le pied de page.
 * - Pages d'accès (data: { pleinEcran: true } dans app.routes.ts) : la page occupe tout l'écran.
 * - Visiteur sur une page publique (annuaire) : barre avec « Se connecter » et « Créer un compte ».
 * - Personne connectée : barre avec ses liens et le menu de son compte.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatButtonModule, MatIconModule, MatMenuModule, MatDividerModule],
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
  readonly estPatient = computed(() => this.profil()?.role === 'PATIENT');

  /** Vrai sur les pages d'accès (connexion, inscription...), qui n'ont ni barre ni pied de page. */
  readonly pleinEcran = toSignal(
    this.router.events.pipe(
      filter((evenement) => evenement instanceof NavigationEnd),
      map(() => pageAffichee(this.router.routerState.snapshot.root).data['pleinEcran'] === true),
    ),
    { initialValue: true },
  );

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

/** La page réellement affichée : la route la plus profonde. */
function pageAffichee(racine: ActivatedRouteSnapshot): ActivatedRouteSnapshot {
  let route = racine;
  while (route.firstChild) {
    route = route.firstChild;
  }
  return route;
}
