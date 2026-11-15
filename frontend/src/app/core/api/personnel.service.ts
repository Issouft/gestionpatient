import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Profil, RolePersonnel } from '../modeles/compte';

/** utilisateur/dto/PersonnelCreationDto.java */
export interface InvitationPersonnel {
  nom: string;
  prenom: string;
  email: string;
  role: RolePersonnel;
  /** Obligatoires pour un médecin. */
  specialite?: string;
  numeroOrdre?: string;
}

/**
 * Le personnel de l'établissement de l'administrateur (permission PERSONNEL_GERER).
 * L'établissement vient du jeton : l'administrateur ne voit jamais le personnel des autres établissements.
 */
@Injectable({ providedIn: 'root' })
export class PersonnelService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/personnel`;

  lister(): Observable<Profil[]> {
    return this.http.get<Profil[]>(this.url);
  }

  /** Compte inactif + email d'invitation. 409 EMAIL_DEJA_UTILISE ou NUMERO_ORDRE_DEJA_UTILISE. */
  inviter(invitation: InvitationPersonnel): Observable<Profil> {
    return this.http.post<Profil>(this.url, invitation);
  }

  /** Nouveau lien d'activation (l'ancien ne marche plus). 409 COMPTE_DEJA_ACTIF. */
  renvoyerInvitation(id: number): Observable<void> {
    return this.http.post<void>(`${this.url}/${id}/invitation`, null);
  }

  /** Suspend la personne dans CET établissement seulement. */
  suspendre(id: number): Observable<void> {
    return this.http.post<void>(`${this.url}/${id}/desactivation`, null);
  }

  reactiver(id: number): Observable<void> {
    return this.http.post<void>(`${this.url}/${id}/reactivation`, null);
  }
}
