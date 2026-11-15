import { Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';

import { ComptePublicService } from '../../../core/api/compte-public.service';
import { SessionService } from '../../../core/auth/session.service';
import { codeErreur, messageErreur } from '../../../core/erreurs/message-erreur';
import { REGLES_MOT_DE_PASSE, appliquerErreursServeur, lierConfirmation } from '../../../core/formulaires/validateurs';
import { BoutonVisibilite } from '../../../shared/bouton-visibilite/bouton-visibilite';
import { PageAcces } from '../../../shared/page-acces/page-acces';

/** Les deux pages où l'on arrive par le lien d'un email. */
export type ModeChoixMotDePasse = 'activation' | 'reinitialisation';

const TEXTES: Record<ModeChoixMotDePasse, { titre: string; intro: string; bouton: string; succes: string; lienExpire: string }> = {
  activation: {
    titre: 'Activer mon compte',
    intro: 'Bienvenue ! Choisissez le mot de passe de votre compte. Vous seul le connaîtrez.',
    bouton: 'Activer mon compte',
    succes: 'Votre compte est activé. Vous pouvez maintenant vous connecter avec votre email et ce mot de passe.',
    lienExpire: 'Ce lien d\'activation a expiré ou a déjà servi. Demandez à votre administrateur de vous renvoyer une invitation.',
  },
  reinitialisation: {
    titre: 'Nouveau mot de passe',
    intro: 'Choisissez votre nouveau mot de passe.',
    bouton: 'Changer mon mot de passe',
    succes: 'Votre mot de passe est changé. Par sécurité, toutes vos sessions ouvertes ont été fermées.',
    lienExpire: 'Ce lien a expiré (il est valable 1 heure) ou a déjà servi. Refaites une demande de mot de passe oublié.',
  },
};

/**
 * Choix d'un mot de passe avec le jeton reçu par email :
 * - activation (POST /auth/activation) : le personnel invité choisit son premier mot de passe ;
 * - réinitialisation (POST /auth/reinitialisation) : après un « mot de passe oublié ».
 * Le mode vient de la route (data), le jeton de l'adresse (?jeton=...).
 */
@Component({
  selector: 'app-choix-mot-de-passe',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    PageAcces,
    BoutonVisibilite,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './choix-mot-de-passe.html',
})
export class ChoixMotDePasse {
  private readonly comptes = inject(ComptePublicService);
  private readonly session = inject(SessionService);

  readonly mode = input.required<ModeChoixMotDePasse>();
  readonly jeton = input<string>();

  readonly textes = computed(() => TEXTES[this.mode()]);
  readonly lienIncomplet = computed(() => !this.jeton()?.trim());
  readonly enCours = signal(false);
  readonly reussi = signal(false);
  readonly erreur = signal<string | null>(null);

  readonly formulaire = inject(NonNullableFormBuilder).group({
    motDePasse: ['', REGLES_MOT_DE_PASSE],
    confirmation: ['', Validators.required],
  });

  constructor() {
    const champs = this.formulaire.controls;
    lierConfirmation(champs.motDePasse, champs.confirmation, inject(DestroyRef));
  }

  valider(): void {
    if (this.formulaire.invalid || this.enCours() || this.lienIncomplet()) {
      this.formulaire.markAllAsTouched();
      return;
    }
    this.enCours.set(true);
    this.erreur.set(null);
    const jeton = this.jeton()!.trim();
    const { motDePasse } = this.formulaire.getRawValue();
    const envoi: Observable<void> = this.mode() === 'activation'
      ? this.comptes.activer(jeton, motDePasse)
      : this.comptes.reinitialiser(jeton, motDePasse);

    envoi.subscribe({
      next: () => {
        // Une session ouverte dans cet onglet n'est plus valable (mot de passe changé) : on la ferme.
        this.session.deconnecter();
        this.reussi.set(true);
      },
      error: (erreur) => {
        this.enCours.set(false);
        if (codeErreur(erreur) === 'JETON_INVALIDE') {
          this.erreur.set(this.textes().lienExpire);
        } else if (!appliquerErreursServeur(this.formulaire, erreur)) {
          this.erreur.set(messageErreur(erreur));
        }
      },
    });
  }
}
