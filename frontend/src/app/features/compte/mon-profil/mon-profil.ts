import { DatePipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';

import { SessionService } from '../../../core/auth/session.service';
import { LIBELLES_ROLES } from '../../../core/modeles/compte';

/**
 * Le profil de la personne connectée (GET /utilisateurs/moi, déjà chargé par la session).
 * Affiche les champs propres au patient ou au médecin quand ils existent. Jamais de mot de passe.
 */
@Component({
  selector: 'app-mon-profil',
  imports: [MatCardModule, MatListModule, DatePipe],
  templateUrl: './mon-profil.html',
})
export class MonProfil {
  readonly profil = inject(SessionService).profil;
  readonly libelleRole = computed(() => {
    const role = this.profil()?.role;
    return role ? LIBELLES_ROLES[role] : '';
  });
}
