import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

import { EtablissementPublic, ICONES_TYPES, LIBELLES_TYPES } from '../../core/modeles/etablissement';

/**
 * Un établissement présenté en carte cliquable (annuaire, mes cliniques) : mène à sa fiche.
 * `rejointe` ajoute l'étiquette « Ma clinique ».
 */
@Component({
  selector: 'app-carte-etablissement',
  imports: [MatIconModule, RouterLink],
  template: `
    @let etab = etablissement();
    <a class="carte-action" [routerLink]="['/annuaire', etab.id]">
      <div class="entete">
        <span class="pastille"><mat-icon>{{ icone() }}</mat-icon></span>
        @if (rejointe()) {
          <span class="etiquette"><mat-icon>check</mat-icon>Ma clinique</span>
        }
      </div>
      <div>
        <h3>{{ etab.nom }}</h3>
        <p class="type">{{ libelleType() }}</p>
      </div>
      @if (etab.ville || etab.adresse) {
        <p class="ligne"><mat-icon>place</mat-icon>{{ lieu() }}</p>
      }
      @if (etab.telephone) {
        <p class="ligne"><mat-icon>call</mat-icon>{{ etab.telephone }}</p>
      }
      <span class="lien-fleche">Voir les médecins <mat-icon>arrow_forward</mat-icon></span>
    </a>
  `,
  styles: `
    :host { display: block; }
    .carte-action { height: 100%; box-sizing: border-box; }
    .entete { display: flex; justify-content: space-between; align-items: flex-start; }
    .etiquette mat-icon { font-size: 16px; width: 16px; height: 16px; }
    .type { margin-top: 2px; font: var(--mat-sys-label-large); color: var(--mat-sys-primary); }
    .ligne { display: flex; align-items: center; gap: 6px; }
    .ligne mat-icon { font-size: 18px; width: 18px; height: 18px; color: var(--mat-sys-on-surface-variant); }
  `,
})
export class CarteEtablissement {
  readonly etablissement = input.required<EtablissementPublic>();
  readonly rejointe = input(false);

  readonly icone = computed(() => ICONES_TYPES[this.etablissement().type]);
  readonly libelleType = computed(() => LIBELLES_TYPES[this.etablissement().type]);
  readonly lieu = computed(() => {
    const { adresse, ville } = this.etablissement();
    return [adresse, ville].filter(Boolean).join(', ');
  });
}
