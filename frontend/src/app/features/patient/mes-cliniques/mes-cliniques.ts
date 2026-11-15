import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { catchError, map, of, startWith } from 'rxjs';

import { PatientService } from '../../../core/api/patient.service';
import { messageErreur } from '../../../core/erreurs/message-erreur';
import { EtablissementPublic } from '../../../core/modeles/etablissement';
import { CarteEtablissement } from '../../../shared/carte-etablissement/carte-etablissement';

type Etat =
  | { etat: 'chargement' }
  | { etat: 'ok'; etablissements: EtablissementPublic[] }
  | { etat: 'erreur'; message: string };

/**
 * Les cliniques que le patient a rejointes (GET /patients/moi/etablissements).
 * Seules ces cliniques peuvent voir ses rendez-vous et son dossier.
 */
@Component({
  selector: 'app-mes-cliniques',
  imports: [RouterLink, CarteEtablissement, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './mes-cliniques.html',
  styles: `
    .etat { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 64px 16px; text-align: center; }
    .etat h2 { font-size: 1.2rem; margin-top: 8px; }
    .etat p { margin: 0 0 12px; color: var(--mat-sys-on-surface-variant); max-width: 420px; }
    .tete { display: flex; flex-wrap: wrap; justify-content: space-between; align-items: flex-end; gap: 16px; }
  `,
})
export class MesCliniques {
  readonly resultat = toSignal(
    inject(PatientService).mesEtablissements().pipe(
      map((etablissements): Etat => ({ etat: 'ok', etablissements })),
      catchError((erreur) => of<Etat>({ etat: 'erreur', message: messageErreur(erreur) })),
      startWith<Etat>({ etat: 'chargement' }),
    ),
    { initialValue: { etat: 'chargement' } as Etat },
  );
}
