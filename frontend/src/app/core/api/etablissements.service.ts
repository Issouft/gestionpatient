import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Profil } from '../modeles/compte';
import { Etablissement, TypeEtablissement } from '../modeles/etablissement';

/** etablissement/dto/EtablissementDto.java (requête) */
export interface NouvelEtablissement {
  nom: string;
  type: TypeEtablissement;
  adresse?: string;
  ville?: string;
  telephone?: string;
  email?: string;
}

/** utilisateur/dto/AdministrateurInvitationDto.java */
export interface InvitationAdministrateur {
  nom: string;
  prenom: string;
  email: string;
}

/**
 * Les établissements de la plateforme, gérés par le super-administrateur (permission ETABLISSEMENT_GERER).
 * Seulement leur fiche : jamais leur personnel, leurs patients ni leurs dossiers.
 */
@Injectable({ providedIn: 'root' })
export class EtablissementsService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/etablissements`;

  lister(): Observable<Etablissement[]> {
    return this.http.get<Etablissement[]>(this.url);
  }

  creer(etablissement: NouvelEtablissement): Observable<Etablissement> {
    return this.http.post<Etablissement>(this.url, etablissement);
  }

  /** Modifie la fiche (mêmes champs et règles qu'à la création). Ne change pas « actif ». */
  modifier(id: number, etablissement: NouvelEtablissement): Observable<Etablissement> {
    return this.http.put<Etablissement>(`${this.url}/${id}`, etablissement);
  }

  /** Tout son personnel perd l'accès tout de suite. Les données restent en base. */
  desactiver(id: number): Observable<void> {
    return this.http.post<void>(`${this.url}/${id}/desactivation`, null);
  }

  reactiver(id: number): Observable<void> {
    return this.http.post<void>(`${this.url}/${id}/reactivation`, null);
  }

  /** Compte administrateur inactif + email d'invitation. 409 EMAIL_DEJA_UTILISE. */
  inviterAdministrateur(etablissementId: number, invitation: InvitationAdministrateur): Observable<Profil> {
    return this.http.post<Profil>(`${this.url}/${etablissementId}/administrateurs`, invitation);
  }
}
