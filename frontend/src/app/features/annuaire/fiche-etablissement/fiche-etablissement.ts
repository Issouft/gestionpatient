import { Component, computed, inject, input, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, startWith, switchMap } from 'rxjs';

import { AnnuaireService } from '../../../core/api/annuaire.service';
import { PatientService } from '../../../core/api/patient.service';
import { SessionService } from '../../../core/auth/session.service';
import { codeErreur, messageErreur } from '../../../core/erreurs/message-erreur';
import {
  EtablissementPublic,
  ICONES_TYPES,
  LIBELLES_TYPES,
  MedecinPublic,
} from '../../../core/modeles/etablissement';

type Fiche =
  | { etat: 'chargement' }
  | { etat: 'ok'; etablissement: EtablissementPublic; medecins: MedecinPublic[]; rejointe: boolean }
  | { etat: 'introuvable' }
  | { etat: 'erreur'; message: string };

/**
 * La fiche publique d'un établissement (GET /annuaire/etablissements/{id}) et ses médecins.
 * Visiteur : peut créer un compte dans cette clinique. Patient : peut la rejoindre. Personnel : consulte seulement.
 */
@Component({
  selector: 'app-fiche-etablissement',
  imports: [RouterLink, MatButtonModule, MatChipsModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './fiche-etablissement.html',
  styleUrl: './fiche-etablissement.scss',
})
export class FicheEtablissement {
  private readonly annuaire = inject(AnnuaireService);
  private readonly patients = inject(PatientService);
  private readonly session = inject(SessionService);
  private readonly snackBar = inject(MatSnackBar);

  /** L'id de l'adresse (/annuaire/:id). */
  readonly id = input.required<string>();

  readonly estVisiteur = computed(() => !this.session.estConnecte());
  readonly estPatient = computed(() => this.session.profil()?.role === 'PATIENT');

  private readonly fiche = toSignal(
    toObservable(this.id).pipe(
      map(Number),
      switchMap((id) =>
        forkJoin({
          etablissement: this.annuaire.fiche(id),
          medecins: this.annuaire.medecins(id),
          // Le patient voit s'il a déjà rejoint la clinique ; pour les autres, la question ne se pose pas.
          mesCliniques: this.estPatient() ? this.patients.mesEtablissements() : of([]),
        }).pipe(
          map(({ etablissement, medecins, mesCliniques }): Fiche => ({
            etat: 'ok',
            etablissement,
            medecins,
            rejointe: mesCliniques.some((etab) => etab.id === id),
          })),
          catchError((erreur) =>
            of<Fiche>(
              codeErreur(erreur) === 'ETABLISSEMENT_INTROUVABLE'
                ? { etat: 'introuvable' }
                : { etat: 'erreur', message: messageErreur(erreur) },
            ),
          ),
          startWith<Fiche>({ etat: 'chargement' }),
        ),
      ),
    ),
    { initialValue: { etat: 'chargement' } as Fiche },
  );

  readonly etat = computed(() => this.fiche().etat);
  readonly etablissement = computed(() => {
    const fiche = this.fiche();
    return fiche.etat === 'ok' ? fiche.etablissement : null;
  });
  readonly medecins = computed(() => {
    const fiche = this.fiche();
    return fiche.etat === 'ok' ? fiche.medecins : [];
  });
  readonly messageErreur = computed(() => {
    const fiche = this.fiche();
    return fiche.etat === 'erreur' ? fiche.message : '';
  });

  /** Rejointe : d'après le chargement, ou juste après avoir cliqué sur « Rejoindre ». */
  private readonly rejointeMaintenant = signal(false);
  readonly rejointe = computed(() => {
    const fiche = this.fiche();
    return this.rejointeMaintenant() || (fiche.etat === 'ok' && fiche.rejointe);
  });
  readonly enCours = signal(false);
  readonly erreurAdhesion = signal<string | null>(null);

  readonly icone = computed(() => {
    const etab = this.etablissement();
    return etab ? ICONES_TYPES[etab.type] : 'local_hospital';
  });
  readonly libelleType = computed(() => {
    const etab = this.etablissement();
    return etab ? LIBELLES_TYPES[etab.type] : '';
  });

  /** Les spécialités présentes, triées, pour le filtre. */
  readonly specialites = computed(() =>
    [...new Set(this.medecins().map((medecin) => medecin.specialite).filter((s): s is string => !!s))].sort((a, b) =>
      a.localeCompare(b, 'fr'),
    ),
  );
  readonly specialiteChoisie = signal<string | null>(null);
  readonly medecinsAffiches = computed(() => {
    const choisie = this.specialiteChoisie();
    return choisie ? this.medecins().filter((medecin) => medecin.specialite === choisie) : this.medecins();
  });

  initiales(medecin: MedecinPublic): string {
    return `${medecin.prenom.charAt(0)}${medecin.nom.charAt(0)}`.toUpperCase();
  }

  rejoindre(): void {
    const etab = this.etablissement();
    if (!etab || this.enCours()) {
      return;
    }
    this.enCours.set(true);
    this.erreurAdhesion.set(null);
    this.patients.rejoindre(etab.id).subscribe({
      next: () => {
        this.enCours.set(false);
        this.rejointeMaintenant.set(true);
        this.snackBar.open(`Vous avez rejoint ${etab.nom}.`, 'OK', { duration: 4000 });
      },
      error: (erreur) => {
        // 403 ADHESION_SUSPENDUE : le message du backend dit de contacter la clinique.
        this.enCours.set(false);
        this.erreurAdhesion.set(messageErreur(erreur));
      },
    });
  }
}
