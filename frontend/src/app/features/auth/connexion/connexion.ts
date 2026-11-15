import { Component, computed, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router } from '@angular/router';

import { SessionService } from '../../../core/auth/session.service';
import { messageErreur } from '../../../core/erreurs/message-erreur';

/**
 * Écran de connexion (POST /auth/connexion).
 * Après la connexion : le personnel qui a plusieurs établissements choisit le sien, les autres vont à l'accueil.
 */
@Component({
  selector: 'app-connexion',
  imports: [ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressBarModule],
  templateUrl: './connexion.html',
  styleUrl: './connexion.scss',
})
export class Connexion {
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  /** ?raison=session-terminee : mis par l'intercepteur quand le backend a refusé le jeton (401). */
  readonly raison = input<string>();
  readonly sessionTerminee = computed(() => this.raison() === 'session-terminee');

  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);

  readonly formulaire = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', Validators.required],
  });

  seConnecter(): void {
    if (this.formulaire.invalid || this.enCours()) {
      this.formulaire.markAllAsTouched();
      return;
    }
    this.enCours.set(true);
    this.erreur.set(null);
    const { email, motDePasse } = this.formulaire.getRawValue();

    this.session.connecter(email, motDePasse).subscribe({
      next: () => {
        const suite = this.session.doitChoisirEtablissement() ? '/choix-etablissement' : '/accueil';
        this.router.navigateByUrl(suite);
      },
      error: (erreur) => {
        // Même message pour un email inconnu et un mot de passe faux : le backend ne dit pas lequel est faux.
        this.erreur.set(messageErreur(erreur));
        this.enCours.set(false);
      },
    });
  }
}
