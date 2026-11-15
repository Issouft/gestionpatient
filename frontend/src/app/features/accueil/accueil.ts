import { Component, computed, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

import { SessionService } from '../../core/auth/session.service';
import { LIBELLES_ROLES, Role } from '../../core/modeles/compte';

/** Une carte de l'accueil. Sans lien, elle est affichée « Bientôt » (écran prévu à une étape suivante). */
interface CarteAccueil {
  icone: string;
  titre: string;
  texte: string;
  lien?: string;
}

/** Les cartes propres à chaque rôle (en plus de « Mon profil », commune à tous). */
const CARTES_PAR_ROLE: Partial<Record<Role, CarteAccueil[]>> = {
  PATIENT: [
    { icone: 'travel_explore', titre: 'Trouver une clinique', texte: 'Parcourez les cliniques et leurs spécialistes.' },
    { icone: 'local_hospital', titre: 'Mes cliniques', texte: 'Les cliniques qui vous suivent.' },
    { icone: 'event_available', titre: 'Mes rendez-vous', texte: 'Prenez et suivez vos rendez-vous.' },
  ],
  ADMINISTRATEUR: [
    { icone: 'groups', titre: 'Personnel', texte: 'Invitez, suspendez et réactivez votre équipe.' },
  ],
  SUPER_ADMINISTRATEUR: [
    { icone: 'domain', titre: 'Établissements', texte: 'Créez les établissements et invitez leurs administrateurs.' },
  ],
};

/**
 * Page d'accueil après la connexion : un bandeau qui rappelle qui est connecté et où,
 * puis les écrans de son rôle sous forme de cartes.
 */
@Component({
  selector: 'app-accueil',
  imports: [MatIconModule, RouterLink],
  templateUrl: './accueil.html',
  styleUrl: './accueil.scss',
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

  readonly cartes = computed<CarteAccueil[]>(() => {
    const role = this.profil()?.role;
    const propres = role ? (CARTES_PAR_ROLE[role] ?? []) : [];
    return [
      ...propres,
      { icone: 'person_outline', titre: 'Mon profil', texte: 'Vos informations personnelles.', lien: '/profil' },
    ];
  });

  /** « Bonjour » le jour, « Bonsoir » à partir de 18 h. */
  readonly salutation = new Date().getHours() >= 18 ? 'Bonsoir' : 'Bonjour';
}
