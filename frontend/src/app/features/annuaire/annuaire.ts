import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { catchError, debounceTime, distinctUntilChanged, map, of, startWith, switchMap } from 'rxjs';

import { AnnuaireService } from '../../core/api/annuaire.service';
import { PatientService } from '../../core/api/patient.service';
import { SessionService } from '../../core/auth/session.service';
import { messageErreur } from '../../core/erreurs/message-erreur';
import { EtablissementPublic, LIBELLES_TYPES, TypeEtablissement } from '../../core/modeles/etablissement';
import { CarteEtablissement } from '../../shared/carte-etablissement/carte-etablissement';

/** Ce que la page affiche : chargement, résultats ou erreur. */
type Resultat =
  | { etat: 'chargement' }
  | { etat: 'ok'; etablissements: EtablissementPublic[] }
  | { etat: 'erreur'; message: string };

/**
 * L'annuaire public (GET /annuaire/etablissements), ouvert aussi aux visiteurs :
 * on cherche un établissement par ville et par type, puis on ouvre sa fiche pour voir ses médecins.
 */
@Component({
  selector: 'app-annuaire',
  imports: [
    ReactiveFormsModule,
    CarteEtablissement,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonToggleModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './annuaire.html',
  styleUrl: './annuaire.scss',
})
export class Annuaire {
  private readonly annuaire = inject(AnnuaireService);
  private readonly session = inject(SessionService);
  private readonly patients = inject(PatientService);

  readonly types = Object.entries(LIBELLES_TYPES) as [TypeEtablissement, string][];

  readonly filtres = inject(NonNullableFormBuilder).group({
    ville: [''],
    type: ['' as TypeEtablissement | ''],
  });

  /**
   * Les résultats suivent les filtres : on attend 300 ms après la dernière frappe,
   * et switchMap annule une recherche dépassée par une plus récente.
   */
  readonly resultat = toSignal(
    this.filtres.valueChanges.pipe(
      startWith(this.filtres.getRawValue()),
      map(() => this.filtres.getRawValue()),
      debounceTime(300),
      distinctUntilChanged((a, b) => a.ville.trim() === b.ville.trim() && a.type === b.type),
      switchMap(({ ville, type }) =>
        this.annuaire.etablissements({ ville, type: type || undefined }).pipe(
          map((etablissements): Resultat => ({ etat: 'ok', etablissements })),
          catchError((erreur) => of<Resultat>({ etat: 'erreur', message: messageErreur(erreur) })),
          startWith<Resultat>({ etat: 'chargement' }),
        ),
      ),
    ),
    { initialValue: { etat: 'chargement' } as Resultat },
  );

  /** Les cliniques déjà rejointes par le patient connecté (vide pour un visiteur ou le personnel). */
  private readonly rejointes = signal<Set<number>>(new Set());
  readonly estRejointe = (id: number) => this.rejointes().has(id);

  readonly chargement = computed(() => this.resultat().etat === 'chargement');
  readonly etablissements = computed(() => {
    const resultat = this.resultat();
    return resultat.etat === 'ok' ? resultat.etablissements : [];
  });
  readonly erreur = computed(() => {
    const resultat = this.resultat();
    return resultat.etat === 'erreur' ? resultat.message : null;
  });

  constructor() {
    if (this.session.profil()?.role === 'PATIENT') {
      this.patients.mesEtablissements().subscribe({
        next: (liste) => this.rejointes.set(new Set(liste.map((etab) => etab.id))),
        error: () => this.rejointes.set(new Set()), // l'étiquette est un confort : la recherche marche sans elle
      });
    }
  }

  effacerFiltres(): void {
    this.filtres.setValue({ ville: '', type: '' });
  }
}
