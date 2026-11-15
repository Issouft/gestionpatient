import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { EtablissementPublic } from '../modeles/etablissement';

/**
 * Les cliniques du patient connecté (permission ETABLISSEMENT_REJOINDRE, donnée au patient seulement).
 * « moi » : le backend prend l'identité dans le jeton, jamais dans l'adresse.
 */
@Injectable({ providedIn: 'root' })
export class PatientService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/patients/moi/etablissements`;

  /** Les cliniques actives que le patient a rejointes, triées par nom. */
  mesEtablissements(): Observable<EtablissementPublic[]> {
    return this.http.get<EtablissementPublic[]>(this.url);
  }

  /**
   * Rejoindre une clinique : elle pourra voir ses rendez-vous et son dossier. 204, même s'il l'avait déjà rejointe.
   * 404 ETABLISSEMENT_INTROUVABLE, 403 ADHESION_SUSPENDUE si la clinique l'a suspendu.
   */
  rejoindre(etablissementId: number): Observable<void> {
    return this.http.post<void>(`${this.url}/${etablissementId}`, null);
  }
}
