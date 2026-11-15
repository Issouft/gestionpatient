/** Types qui reprennent les DTO publics de l'annuaire (backend : EtablissementPublicDto, MedecinPublicDto). */

/** etablissement/enums/TypeEtablissement.java */
export type TypeEtablissement = 'CLINIQUE' | 'CABINET' | 'PHARMACIE' | 'LABORATOIRE';

export const LIBELLES_TYPES: Record<TypeEtablissement, string> = {
  CLINIQUE: 'Clinique',
  CABINET: 'Cabinet',
  PHARMACIE: 'Pharmacie',
  LABORATOIRE: 'Laboratoire',
};

/** L'icône Material de chaque type d'établissement. */
export const ICONES_TYPES: Record<TypeEtablissement, string> = {
  CLINIQUE: 'local_hospital',
  CABINET: 'medical_services',
  PHARMACIE: 'local_pharmacy',
  LABORATOIRE: 'biotech',
};

/** etablissement/dto/EtablissementPublicDto.java : la fiche publique d'un établissement actif. */
export interface EtablissementPublic {
  id: number;
  nom: string;
  type: TypeEtablissement;
  adresse: string | null;
  ville: string | null;
  telephone: string | null;
  email: string | null;
}

/** utilisateur/dto/MedecinPublicDto.java : un médecin dans l'annuaire (ni email ni numéro d'ordre). */
export interface MedecinPublic {
  id: number;
  nom: string;
  prenom: string;
  specialite: string | null;
}
