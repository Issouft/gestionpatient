import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, firstValueFrom, map, of, switchMap, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AccesEtablissement, ConnexionReponse, Profil, agitSansEtablissement } from '../modeles/compte';

/** Ce qui est gardé dans l'onglet entre deux rechargements de page. Jamais de mot de passe. */
interface SessionEnregistree {
  jeton: string;
  etablissementId: number | null;
  etablissements: AccesEtablissement[];
}

const CLE_STOCKAGE = 'gestionpatient.session';

/**
 * La personne connectée : son jeton, son profil et l'établissement où elle travaille.
 * C'est le SEUL endroit qui connaît la session ; les écrans lisent ses signals et se mettent à jour tout seuls.
 *
 * Sécurité :
 * - le jeton est gardé dans sessionStorage : il disparaît à la fermeture de l'onglet ;
 * - le rôle affiché vient de GET /utilisateurs/moi, mais c'est le backend qui vérifie les droits à chaque requête :
 *   cacher un bouton ne protège rien, c'est seulement du confort.
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly http = inject(HttpClient);

  private readonly session = signal<SessionEnregistree | null>(lireSession());
  private readonly profilCharge = signal<Profil | null>(null);

  /** Le jeton à envoyer au backend, ou null si personne n'est connecté. */
  readonly jeton = computed(() => this.session()?.jeton ?? null);
  readonly profil = this.profilCharge.asReadonly();
  readonly estConnecte = computed(() => this.jeton() !== null && this.profilCharge() !== null);

  /** Les établissements où le personnel travaille (vide pour un patient ou le super-administrateur). */
  readonly etablissements = computed(() => this.session()?.etablissements ?? []);

  /** L'établissement de la session, ou undefined s'il n'y en a pas. */
  readonly etablissementCourant = computed(() => {
    const id = this.session()?.etablissementId;
    return this.etablissements().find((acces) => acces.etablissementId === id);
  });

  /** Vrai pour le personnel qui travaille dans plusieurs établissements et n'a pas encore choisi. */
  readonly doitChoisirEtablissement = computed(() => {
    const profil = this.profilCharge();
    return profil !== null
      && !agitSansEtablissement(profil.role)
      && this.etablissementCourant() === undefined
      && this.etablissements().length > 1;
  });

  /** Vrai pour le personnel qui n'a accès à aucun établissement (tous suspendus ou désactivés). */
  readonly sansEtablissement = computed(() => {
    const profil = this.profilCharge();
    return profil !== null && !agitSansEtablissement(profil.role) && this.etablissements().length === 0;
  });

  /** POST /auth/connexion, puis chargement du profil. En cas d'erreur, rien n'est gardé. */
  connecter(email: string, motDePasse: string): Observable<Profil> {
    return this.http
      .post<ConnexionReponse>(`${environment.apiUrl}/auth/connexion`, { email, motDePasse })
      .pipe(
        tap((reponse) => this.enregistrer(reponse)),
        switchMap(() => this.chargerProfil()),
        catchError((erreur) => {
          this.deconnecter();
          throw erreur;
        }),
      );
  }

  /** POST /auth/etablissement : nouveau jeton pour travailler dans cet établissement (choix ou changement). */
  choisirEtablissement(etablissementId: number): Observable<void> {
    return this.http
      .post<ConnexionReponse>(`${environment.apiUrl}/auth/etablissement`, { etablissementId })
      .pipe(
        tap((reponse) => this.enregistrer(reponse)),
        map(() => undefined),
      );
  }

  /** GET /utilisateurs/moi : le profil de la personne connectée. */
  chargerProfil(): Observable<Profil> {
    return this.http
      .get<Profil>(`${environment.apiUrl}/utilisateurs/moi`)
      .pipe(tap((profil) => this.profilCharge.set(profil)));
  }

  /**
   * Au démarrage de l'application : si un jeton est resté dans l'onglet, on recharge le profil.
   * S'il a expiré ou a été révoqué, on repart sans session. Ne bloque jamais le démarrage.
   */
  restaurer(): Promise<void> {
    if (this.jeton() === null) {
      return Promise.resolve();
    }
    return firstValueFrom(
      this.chargerProfil().pipe(
        map(() => undefined),
        catchError(() => {
          this.deconnecter();
          return of(undefined);
        }),
      ),
    );
  }

  /** Oublie la session dans l'application et dans l'onglet. */
  deconnecter(): void {
    this.session.set(null);
    this.profilCharge.set(null);
    ecrireSession(null);
  }

  private enregistrer(reponse: ConnexionReponse): void {
    const session: SessionEnregistree = {
      jeton: reponse.jeton,
      etablissementId: reponse.etablissementId,
      etablissements: reponse.etablissements,
    };
    this.session.set(session);
    ecrireSession(session);
  }
}

// sessionStorage peut être indisponible (navigation privée stricte) : l'application marche alors sans le garder.
function lireSession(): SessionEnregistree | null {
  try {
    const texte = sessionStorage.getItem(CLE_STOCKAGE);
    return texte ? (JSON.parse(texte) as SessionEnregistree) : null;
  } catch {
    return null;
  }
}

function ecrireSession(session: SessionEnregistree | null): void {
  try {
    if (session === null) {
      sessionStorage.removeItem(CLE_STOCKAGE);
    } else {
      sessionStorage.setItem(CLE_STOCKAGE, JSON.stringify(session));
    }
  } catch {
    // Rien à faire : la session reste en mémoire jusqu'au rechargement de la page.
  }
}
