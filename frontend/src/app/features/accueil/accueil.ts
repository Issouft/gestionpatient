import { Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

import { SessionService } from '../../core/auth/session.service';
import { LIBELLES_ROLES } from '../../core/modeles/compte';

/**
 * Page d'accueil après la connexion : rappelle qui est connecté et où, et mène aux écrans de son rôle.
 * Les écrans de chaque rôle (patient, administrateur, super-administrateur) s'ajouteront ici aux étapes suivantes.
 */
@Component({
  selector: 'app-accueil',
  imports: [MatCardModule, MatButtonModule, MatIconModule, RouterLink],
  templateUrl: './accueil.html',
})
export class Accueil {
  private readonly session = inject(SessionService);

  readonly profil = this.session.profil;
  readonly etablissement = this.session.etablissementCourant;
  readonly sansEtablissement = this.session.sansEtablissement;
  readonly libelleRole = computed(() => {
    const role = this.profil()?.role;
    return role ? LIBELLES_ROLES[role] : '';
  });
}
