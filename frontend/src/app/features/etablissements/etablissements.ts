import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, filter } from 'rxjs';

import { EtablissementsService } from '../../core/api/etablissements.service';
import { messageErreur } from '../../core/erreurs/message-erreur';
import { Profil } from '../../core/modeles/compte';
import { Etablissement, ICONES_TYPES, LIBELLES_TYPES } from '../../core/modeles/etablissement';
import { Confirmation } from '../../shared/confirmation/confirmation';
import { CreationEtablissementDialogue } from './creation-etablissement/creation-etablissement';
import { InvitationAdministrateurDialogue } from './invitation-administrateur/invitation-administrateur';

/**
 * Les établissements de la plateforme, pour le super-administrateur (routes /etablissements) :
 * création, désactivation, réactivation et invitation de leur administrateur.
 * Il ne voit que leur fiche, jamais leurs données (décision du groupe).
 */
@Component({
  selector: 'app-etablissements',
  imports: [
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatMenuModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './etablissements.html',
})
export class Etablissements {
  private readonly service = inject(EtablissementsService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly icones = ICONES_TYPES;
  readonly libellesTypes = LIBELLES_TYPES;

  readonly etablissements = signal<Etablissement[]>([]);
  readonly chargement = signal(true);
  readonly erreur = signal<string | null>(null);
  readonly actionEnCours = signal<number | null>(null);
  readonly recherche = signal('');

  readonly affiches = computed(() => {
    const texte = this.recherche().trim().toLowerCase();
    return this.etablissements().filter(
      (etab) => !texte || `${etab.nom} ${etab.ville ?? ''}`.toLowerCase().includes(texte),
    );
  });
  readonly nombreActifs = computed(() => this.etablissements().filter((etab) => etab.actif).length);

  constructor() {
    this.charger();
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.service.lister().subscribe({
      next: (liste) => {
        this.etablissements.set(liste);
        this.chargement.set(false);
      },
      error: (erreur) => {
        this.erreur.set(messageErreur(erreur));
        this.chargement.set(false);
      },
    });
  }

  creer(): void {
    this.dialog
      .open(CreationEtablissementDialogue, { autoFocus: 'first-tabbable' })
      .afterClosed()
      .pipe(filter((cree): cree is Etablissement => !!cree))
      .subscribe((cree) => {
        this.snackBar.open(`${cree.nom} est créé. Invitez maintenant son administrateur.`, 'OK', { duration: 5000 });
        this.charger();
      });
  }

  modifier(etab: Etablissement): void {
    this.dialog
      .open(CreationEtablissementDialogue, { data: etab, autoFocus: 'first-tabbable' })
      .afterClosed()
      .pipe(filter((modifie): modifie is Etablissement => !!modifie))
      .subscribe((modifie) => {
        this.snackBar.open(`La fiche de ${modifie.nom} est enregistrée.`, 'OK', { duration: 4000 });
        this.charger();
      });
  }

  inviterAdministrateur(etab: Etablissement): void {
    this.dialog
      .open(InvitationAdministrateurDialogue, { data: etab, autoFocus: 'first-tabbable' })
      .afterClosed()
      .pipe(filter((compte): compte is Profil => !!compte))
      .subscribe((compte) => this.snackBar.open(`Invitation envoyée à ${compte.email}.`, 'OK', { duration: 5000 }));
  }

  desactiver(etab: Etablissement): void {
    this.dialog
      .open(Confirmation, {
        data: {
          titre: `Désactiver ${etab.nom} ?`,
          message: 'Tout son personnel perd l\'accès dès maintenant, et l\'établissement disparaît de l\'annuaire. Ses données restent en base : vous pourrez le réactiver.',
          boutonConfirmer: 'Désactiver',
          danger: true,
        },
      })
      .afterClosed()
      .pipe(filter((oui) => oui === true))
      .subscribe(() => this.agir(etab, this.service.desactiver(etab.id), `${etab.nom} est désactivé.`));
  }

  reactiver(etab: Etablissement): void {
    this.agir(etab, this.service.reactiver(etab.id), `${etab.nom} est réactivé.`);
  }

  private agir(etab: Etablissement, action: Observable<void>, succes: string): void {
    this.actionEnCours.set(etab.id);
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
