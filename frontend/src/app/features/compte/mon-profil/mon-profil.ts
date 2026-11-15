import { formatDate } from '@angular/common';
import { Component, LOCALE_ID, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { filter } from 'rxjs';

import { SessionService } from '../../../core/auth/session.service';
import { LIBELLES_ROLES, Profil } from '../../../core/modeles/compte';
import { ChangementMotDePasseDialogue } from '../changement-mot-de-passe/changement-mot-de-passe';
import { ModificationProfilDialogue } from '../modification-profil/modification-profil';

/** Une information du profil, affichée en tuile. */
interface Information {
  icone: string;
  libelle: string;
  valeur: string;
}

/**
 * Le profil de la personne connectée (GET /utilisateurs/moi, déjà chargé par la session).
 * Affiche les champs propres au patient ou au médecin quand ils existent. Jamais de mot de passe.
 * Permet de modifier ses informations et de changer son mot de passe.
 */
@Component({
  selector: 'app-mon-profil',
  imports: [MatIconModule, MatButtonModule],
  templateUrl: './mon-profil.html',
  styleUrl: './mon-profil.scss',
})
export class MonProfil {
  private readonly locale = inject(LOCALE_ID);
  private readonly session = inject(SessionService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  readonly profil = this.session.profil;

  readonly libelleRole = computed(() => {
    const role = this.profil()?.role;
    return role ? LIBELLES_ROLES[role] : '';
  });

  readonly initiales = computed(() => {
    const profil = this.profil();
    return profil ? `${profil.prenom.charAt(0)}${profil.nom.charAt(0)}`.toUpperCase() : '';
  });

  /** Les tuiles à afficher : seulement les champs qui existent pour ce rôle. */
  readonly informations = computed<Information[]>(() => {
    const profil = this.profil();
    return profil ? this.construire(profil) : [];
  });

  modifier(): void {
    this.dialog
      .open(ModificationProfilDialogue, { autoFocus: 'first-tabbable' })
      .afterClosed()
      .pipe(filter((profil) => !!profil))
      .subscribe(() => this.snackBar.open('Vos informations sont enregistrées.', 'OK', { duration: 4000 }));
  }

  changerMotDePasse(): void {
    this.dialog
      .open(ChangementMotDePasseDialogue, { autoFocus: 'first-tabbable' })
      .afterClosed()
      .pipe(filter((fait) => fait === true))
      .subscribe(() => {
        // Le backend a fermé toutes les sessions, celle-ci comprise : on se reconnecte.
        this.session.deconnecter();
        this.router.navigate(['/connexion'], { queryParams: { raison: 'mot-de-passe-change' } });
      });
  }

  private construire(profil: Profil): Information[] {
    const infos: Information[] = [{ icone: 'mail_outline', libelle: 'Email', valeur: profil.email }];
    if (profil.dateNaissance) {
      const date = formatDate(profil.dateNaissance, 'longDate', this.locale);
      const age = profil.age !== undefined ? ` (${profil.age} ans)` : '';
      infos.push({ icone: 'cake', libelle: 'Date de naissance', valeur: date + age });
    }
    if (profil.sexe) {
      infos.push({ icone: 'wc', libelle: 'Sexe', valeur: profil.sexe === 'FEMININ' ? 'Féminin' : 'Masculin' });
    }
    if (profil.telephone) {
      infos.push({ icone: 'phone', libelle: 'Téléphone', valeur: profil.telephone });
    }
    if (profil.adresse) {
      infos.push({ icone: 'home', libelle: 'Adresse', valeur: profil.adresse });
    }
    if (profil.specialite) {
      infos.push({ icone: 'medical_services', libelle: 'Spécialité', valeur: profil.specialite });
    }
    if (profil.numeroOrdre) {
      infos.push({ icone: 'workspace_premium', libelle: 'Numéro d\'ordre', valeur: profil.numeroOrdre });
    }
    return infos;
  }
}
