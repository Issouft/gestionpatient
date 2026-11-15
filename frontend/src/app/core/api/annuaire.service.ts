import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { EtablissementPublic, MedecinPublic, TypeEtablissement } from '../modeles/etablissement';

/** L'annuaire public (GET /annuaire/**) : consultable sans connexion. */
@Injectable({ providedIn: 'root' })
export class AnnuaireService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/annuaire/etablissements`;

  /** Les établissements actifs, triés par nom. Les filtres vides sont ignorés. */
  etablissements(filtres: { type?: TypeEtablissement; ville?: string } = {}): Observable<EtablissementPublic[]> {
    let params = new HttpParams();
    if (filtres.type) {
      params = params.set('type', filtres.type);
    }
    if (filtres.ville?.trim()) {
      params = params.set('ville', filtres.ville.trim());
    }
    return this.http.get<EtablissementPublic[]>(this.url, { params });
  }

  /** Les médecins en activité d'un établissement, filtrés par spécialité si on la donne. */
  medecins(etablissementId: number, specialite?: string): Observable<MedecinPublic[]> {
    const params = specialite?.trim() ? new HttpParams().set('specialite', specialite.trim()) : undefined;
    return this.http.get<MedecinPublic[]>(`${this.url}/${etablissementId}/medecins`, { params });
  }
}
