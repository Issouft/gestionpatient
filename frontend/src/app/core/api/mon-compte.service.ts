import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Profil } from '../modeles/compte';

/** utilisateur/dto/ModificationProfilDto.java : téléphone et adresse ne servent qu'au patient. */
export interface ModificationProfil {
  nom: string;
  prenom: string;
  telephone?: string;
  adresse?: string;
}

/** Le compte de la personne connectée (GET/PUT /utilisateurs/moi). */
@Injectable({ providedIn: 'root' })
export class MonCompteService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/utilisateurs/moi`;

  modifierProfil(modification: ModificationProfil): Observable<Profil> {
    return this.http.put<Profil>(this.url, modification);
  }

  /** 400 MOT_DE_PASSE_INCORRECT si l'ancien est faux. Réussi : TOUTES les sessions sont fermées, y compris celle-ci. */
  changerMotDePasse(ancienMotDePasse: string, nouveauMotDePasse: string): Observable<void> {
    return this.http.put<void>(`${this.url}/mot-de-passe`, { ancienMotDePasse, nouveauMotDePasse });
  }
}
