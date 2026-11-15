import { Component, DestroyRef, OnInit, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';

import { AnnuaireService } from '../../../core/api/annuaire.service';
import { ComptePublicService, InscriptionPatient } from '../../../core/api/compte-public.service';
import { SessionService } from '../../../core/auth/session.service';
import { codeErreur, messageErreur } from '../../../core/erreurs/message-erreur';
import {
  REGLES_MOT_DE_PASSE,
  REGLE_TELEPHONE,
  appliquerErreursServeur,
  aujourdhui,
  dateDansLePasse,
  lierConfirmation,
} from '../../../core/formulaires/validateurs';
import { EtablissementPublic, LIBELLES_TYPES } from '../../../core/modeles/etablissement';
import { BoutonVisibilite } from '../../../shared/bouton-visibilite/bouton-visibilite';
import { PageAcces } from '../../../shared/page-acces/page-acces';

/**
 * Inscription d'un patient (POST /auth/inscription), puis connexion automatique.
 * La clinique est facultative : choisie dans l'annuaire, ou plus tard depuis l'espace patient.
 * ?etablissement=ID (lien « S'inscrire » de l'annuaire) la présélectionne.
 */
@Component({
  selector: 'app-inscription',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    PageAcces,
    BoutonVisibilite,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonToggleModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './inscription.html',
})
export class Inscription implements OnInit {
  private readonly comptes = inject(ComptePublicService);
  private readonly annuaire = inject(AnnuaireService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  /** ?etablissement=ID : clinique présélectionnée. */
  readonly etablissement = input<string>();

  readonly dateMax = aujourdhui();
  readonly libellesTypes = LIBELLES_TYPES;
  readonly etablissements = signal<EtablissementPublic[]>([]);
  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);

  readonly formulaire = inject(NonNullableFormBuilder).group({
    nom: ['', [Validators.required, Validators.maxLength(255)]],
    prenom: ['', [Validators.required, Validators.maxLength(255)]],
    dateNaissance: ['', [Validators.required, dateDansLePasse]],
    sexe: ['' as '' | 'MASCULIN' | 'FEMININ', Validators.required],
    telephone: ['', [Validators.required, REGLE_TELEPHONE]],
    adresse: ['', [Validators.required, Validators.maxLength(255)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    motDePasse: ['', REGLES_MOT_DE_PASSE],
    confirmation: ['', Validators.required],
    etablissementId: [null as number | null],
  });

  constructor() {
    const champs = this.formulaire.controls;
    lierConfirmation(champs.motDePasse, champs.confirmation, inject(DestroyRef));
  }

  ngOnInit(): void {
    this.chargerEtablissements();
  }

  private chargerEtablissements(): void {
    // L'annuaire est public : la liste se charge sans compte. S'il ne répond pas, on peut s'inscrire sans clinique.
    this.annuaire.etablissements().subscribe({
      next: (liste) => {
        this.etablissements.set(liste);
        const choisi = Number(this.etablissement());
        if (liste.some((etab) => etab.id === choisi)) {
          this.formulaire.controls.etablissementId.setValue(choisi);
        }
      },
      error: () => this.etablissements.set([]),
    });
  }

  sInscrire(): void {
    if (this.formulaire.invalid || this.enCours()) {
      this.formulaire.markAllAsTouched();
      return;
    }
    this.enCours.set(true);
    this.erreur.set(null);

    const { confirmation, sexe, ...champs } = this.formulaire.getRawValue();
    const inscription: InscriptionPatient = {
      ...champs,
      sexe: sexe as 'MASCULIN' | 'FEMININ',
      nom: champs.nom.trim(),
      prenom: champs.prenom.trim(),
      email: champs.email.trim(),
    };

    this.comptes
      .inscrire(inscription)
      .pipe(switchMap(() => this.session.connecter(inscription.email, inscription.motDePasse)))
      .subscribe({
        next: () => this.router.navigateByUrl('/accueil'),
        error: (erreur) => {
          this.enCours.set(false);
          this.afficherErreur(erreur);
        },
      });
  }

  private afficherErreur(erreur: unknown): void {
    const champs = this.formulaire.controls;
    switch (codeErreur(erreur)) {
      case 'EMAIL_DEJA_UTILISE':
        champs.email.setErrors({ serveur: 'Cet email a déjà un compte. Connectez-vous, ou choisissez un autre email.' });
        champs.email.markAsTouched();
        return;
      case 'ETABLISSEMENT_INTROUVABLE':
        // La clinique a été désactivée entre-temps : on recharge la liste.
        champs.etablissementId.setErrors({ serveur: 'Cette clinique n\'est plus disponible. Choisissez-en une autre.' });
        champs.etablissementId.markAsTouched();
        this.chargerEtablissements();
        return;
    }
    if (!appliquerErreursServeur(this.formulaire, erreur)) {
      this.erreur.set(messageErreur(erreur));
    }
  }
}
