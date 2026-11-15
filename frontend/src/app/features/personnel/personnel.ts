import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, filter } from 'rxjs';

import { PersonnelService } from '../../core/api/personnel.service';
import { SessionService } from '../../core/auth/session.service';
import { messageErreur } from '../../core/erreurs/message-erreur';
import { LIBELLES_ROLES, Profil, ROLES_PERSONNEL, RolePersonnel } from '../../core/modeles/compte';
import { Confirmation, DonneesConfirmation } from '../../shared/confirmation/confirmation';
import { InvitationPersonnelDialogue } from './invitation-personnel/invitation-personnel';

/** Le statut d'une personne, tel qu'affiché. */
export type StatutMembre = 'actif' | 'attente' | 'suspendu';

export function statutDe(membre: Profil): StatutMembre {
  if (membre.statutDansEtablissement === 'SUSPENDUE') {
    return 'suspendu';
  }
  return membre.actif ? 'actif' : 'attente';
}

const LIBELLES_STATUTS: Record<StatutMembre, string> = {
  actif: 'Actif',
  attente: 'Invitation en attente',
  suspendu: 'Suspendu',
};

/**
 * Le personnel de l'établissement de l'administrateur : liste, invitation, renvoi d'invitation,
 * suspension et réactivation (routes /personnel). Tout se limite à l'établissement de la session.
 */
@Component({
  selector: 'app-personnel',
  imports: [
    FormsModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatMenuModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './personnel.html',
})
export class Personnel {
  private readonly personnel = inject(PersonnelService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly etablissement = inject(SessionService).etablissementCourant;
  readonly libellesRoles = LIBELLES_ROLES;
  readonly libellesStatuts = LIBELLES_STATUTS;
  readonly roles = ROLES_PERSONNEL;

  readonly membres = signal<Profil[]>([]);
  readonly chargement = signal(true);
  readonly erreur = signal<string | null>(null);
  /** L'id de la personne dont une action est en cours (pour désactiver son menu). */
  readonly actionEnCours = signal<number | null>(null);

  readonly recherche = signal('');
  readonly roleFiltre = signal<RolePersonnel | ''>('');

  readonly membresAffiches = computed(() => {
    const texte = this.recherche().trim().toLowerCase();
    const role = this.roleFiltre();
    return this.membres().filter(
      (membre) =>
        (!role || membre.role === role)
        && (!texte || `${membre.prenom} ${membre.nom} ${membre.email}`.toLowerCase().includes(texte)),
    );
  });

  readonly compteurs = computed(() => {
    const statuts = this.membres().map(statutDe);
    return {
      total: statuts.length,
      actifs: statuts.filter((s) => s === 'actif').length,
      attente: statuts.filter((s) => s === 'attente').length,
      suspendus: statuts.filter((s) => s === 'suspendu').length,
    };
  });

  readonly statutDe = statutDe;

  constructor() {
    this.charger();
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.personnel.lister().subscribe({
      next: (liste) => {
        this.membres.set(liste);
        this.chargement.set(false);
      },
      error: (erreur) => {
        this.erreur.set(messageErreur(erreur));
        this.chargement.set(false);
      },
    });
  }

  initiales(membre: Profil): string {
    return `${membre.prenom.charAt(0)}${membre.nom.charAt(0)}`.toUpperCase();
  }

  inviter(): void {
    this.dialog
      .open(InvitationPersonnelDialogue, { autoFocus: 'first-tabbable' })
      .afterClosed()
      .pipe(filter((compte): compte is Profil => !!compte))
      .subscribe((compte) => {
        this.snackBar.open(`Invitation envoyée à ${compte.email}.`, 'OK', { duration: 5000 });
        this.charger();
      });
  }

  renvoyerInvitation(membre: Profil): void {
    this.agir(membre, this.personnel.renvoyerInvitation(membre.id), `Nouvelle invitation envoyée à ${membre.email}.`);
  }

  suspendre(membre: Profil): void {
    this.confirmer({
      titre: `Suspendre ${membre.prenom} ${membre.nom} ?`,
      message: 'La personne perd l\'accès à votre établissement dès maintenant. Ses autres établissements ne changent pas, et vous pourrez la réactiver.',
      boutonConfirmer: 'Suspendre',
      danger: true,
    }).subscribe(() => this.agir(membre, this.personnel.suspendre(membre.id), `${membre.prenom} ${membre.nom} est suspendu(e).`));
  }

  reactiver(membre: Profil): void {
    this.agir(membre, this.personnel.reactiver(membre.id), `${membre.prenom} ${membre.nom} a retrouvé l'accès.`);
  }

  private confirmer(donnees: DonneesConfirmation): Observable<true> {
    return this.dialog
      .open(Confirmation, { data: donnees })
      .afterClosed()
      .pipe(filter((oui): oui is true => oui === true));
  }

  /** Lance l'action, affiche le résultat, puis recharge la liste. */
  private agir(membre: Profil, action: Observable<void>, succes: string): void {
    this.actionEnCours.set(membre.id);
    action.subscribe({
      next: () => {
        this.actionEnCours.set(null);
        this.snackBar.open(succes, 'OK', { duration: 4000 });
        this.charger();
      },
      error: (erreur) => {
        this.actionEnCours.set(null);
        this.snackBar.open(messageErreur(erreur), 'OK', { duration: 6000 });
      },
    });
  }
}
