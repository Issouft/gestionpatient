/**
 * Types qui reprennent EXACTEMENT les DTO du backend (dossier dto/ de chaque module Java).
 * Si un DTO change côté Java, il faut changer le type ici : le compilateur signalera les écrans concernés.
 */

/** utilisateur/enums/Role.java */
export type Role =
  | 'PATIENT'
  | 'MEDECIN'
  | 'SECRETAIRE'
  | 'PHARMACIEN'
  | 'ADMINISTRATEUR'
  | 'SUPER_ADMINISTRATEUR';

/** etablissement/dto/AccesEtablissementDto.java : un établissement où le personnel travaille. */
export interface AccesEtablissement {
  etablissementId: number;
  etablissementNom: string;
  roles: Role[];
}

/** securite/dto/ConnexionReponse.java : réponse de POST /auth/connexion et POST /auth/etablissement. */
export interface ConnexionReponse {
  jeton: string;
  /** null : patient, super-administrateur, ou personnel qui doit encore choisir son établissement. */
  etablissementId: number | null;
  etablissements: AccesEtablissement[];
}

/**
 * utilisateur/dto/UtilisateurDto.java (+ PatientDto, MedecinDto) : réponse de GET /utilisateurs/moi.
 * Les champs facultatifs n'existent que pour certains rôles.
 */
export interface Profil {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: Role;
  actif: boolean;
  // Patient
  dateNaissance?: string;
  age?: number;
  sexe?: 'MASCULIN' | 'FEMININ';
  telephone?: string;
  adresse?: string;
  // Médecin
  specialite?: string;
  numeroOrdre?: string;
}

/** Les rôles qui n'ont jamais d'établissement à choisir (comme Role.agitSansEtablissement() en Java). */
export function agitSansEtablissement(role: Role): boolean {
  return role === 'PATIENT' || role === 'SUPER_ADMINISTRATEUR';
}

/** Le nom à afficher pour un rôle. */
export const LIBELLES_ROLES: Record<Role, string> = {
  PATIENT: 'Patient',
  MEDECIN: 'Médecin',
  SECRETAIRE: 'Secrétaire',
  PHARMACIEN: 'Pharmacien',
  ADMINISTRATEUR: 'Administrateur',
  SUPER_ADMINISTRATEUR: 'Super-administrateur',
};
