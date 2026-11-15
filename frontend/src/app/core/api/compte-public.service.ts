import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Profil } from '../modeles/compte';

/** utilisateur/dto/PatientInscriptionDto.java */
export interface InscriptionPatient {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  dateNaissance: string; // AAAA-MM-JJ
  sexe: 'MASCULIN' | 'FEMININ';
  telephone: string;
  adresse: string;
  /** Facultatif : la clinique choisie dans l'annuaire. */
  etablissementId?: number | null;
}

/**
 * Les routes publiques des comptes (POST /auth/...), utilisées SANS jeton :
 * inscription, activation d'un compte invité, mot de passe oublié et réinitialisation.
 */
@Injectable({ providedIn: 'root' })
export class ComptePublicService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/auth`;

  /** 201 : le compte patient est créé (actif). 409 EMAIL_DEJA_UTILISE, 404 si la clinique n'existe plus. */
  inscrire(inscription: InscriptionPatient): Observable<Profil> {
    return this.http.post<Profil>(`${this.url}/inscription`, inscription);
  }

  /** Le personnel invité choisit son mot de passe avec le jeton du lien reçu par email (valable 48 h, une fois). */
  activer(jeton: string, motDePasse: string): Observable<void> {
    return this.http.post<void>(`${this.url}/activation`, { jeton, motDePasse });
  }

  /** Toujours 204 : la réponse ne dit pas si l'email a un compte. */
  motDePasseOublie(email: string): Observable<void> {
    return this.http.post<void>(`${this.url}/mot-de-passe-oublie`, { email });
  }

  /** Nouveau mot de passe avec le jeton du lien (valable 1 h, une fois). Ferme toutes les sessions ouvertes. */
  reinitialiser(jeton: string, motDePasse: string): Observable<void> {
    return this.http.post<void>(`${this.url}/reinitialisation`, { jeton, motDePasse });
  }
}
