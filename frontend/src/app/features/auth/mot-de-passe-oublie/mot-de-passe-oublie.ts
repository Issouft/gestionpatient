import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';

import { ComptePublicService } from '../../../core/api/compte-public.service';
import { messageErreur } from '../../../core/erreurs/message-erreur';
import { PageAcces } from '../../../shared/page-acces/page-acces';

/**
 * Mot de passe oublié (POST /auth/mot-de-passe-oublie) : la personne reçoit un lien valable 1 heure.
 * Le message de réussite est le même que l'email ait un compte ou non : on ne révèle pas quels comptes existent.
 */
@Component({
  selector: 'app-mot-de-passe-oublie',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    PageAcces,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './mot-de-passe-oublie.html',
})
export class MotDePasseOublie {
  private readonly comptes = inject(ComptePublicService);

  readonly enCours = signal(false);
  readonly emailEnvoye = signal<string | null>(null);
  readonly erreur = signal<string | null>(null);

  readonly formulaire = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email]],
  });

  envoyer(): void {
    if (this.formulaire.invalid || this.enCours()) {
      this.formulaire.markAllAsTouched();
      return;
    }
    this.enCours.set(true);
    this.erreur.set(null);
    const email = this.formulaire.getRawValue().email.trim();

    this.comptes.motDePasseOublie(email).subscribe({
      next: () => this.emailEnvoye.set(email),
      error: (erreur) => {
        // Le backend répond toujours 204 : une erreur ici vient du réseau ou d'une saisie invalide.
        this.erreur.set(messageErreur(erreur));
        this.enCours.set(false);
      },
    });
  }
}
