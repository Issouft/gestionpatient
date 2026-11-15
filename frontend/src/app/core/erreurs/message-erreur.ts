import { HttpErrorResponse } from '@angular/common/http';

/**
 * Forme des erreurs du backend (ProblemDetail, voir GlobalExceptionHandler.java) :
 * { "status": 409, "title": "...", "detail": "...", "code": "EMAIL_DEJA_UTILISE", "erreurs": { "champ": "message" } }
 */
export interface ErreurApi {
  status?: number;
  title?: string;
  detail?: string;
  code?: string;
  erreurs?: Record<string, string>;
}

/** Le code de l'erreur (ex. "EMAIL_DEJA_UTILISE"), pour réagir à un cas précis. undefined si absent. */
export function codeErreur(erreur: unknown): string | undefined {
  return erreurApi(erreur)?.code;
}

/** Les messages par champ d'une erreur DONNEES_INVALIDES, pour les afficher sous chaque champ du formulaire. */
export function erreursParChamp(erreur: unknown): Record<string, string> {
  return erreurApi(erreur)?.erreurs ?? {};
}

/**
 * Le message à montrer à l'utilisateur, en une phrase. Chaque écran l'utilise : les erreurs s'affichent
 * partout de la même façon, et un nouveau code d'erreur du backend s'affiche sans rien changer ici.
 */
export function messageErreur(erreur: unknown): string {
  if (erreur instanceof HttpErrorResponse && erreur.status === 0) {
    return 'Le serveur ne répond pas. Vérifiez que l\'API est démarrée, puis réessayez.';
  }
  const api = erreurApi(erreur);
  if (api?.detail) {
    return api.detail;
  }
  return 'Une erreur inattendue est survenue. Réessayez dans quelques instants.';
}

function erreurApi(erreur: unknown): ErreurApi | undefined {
  if (erreur instanceof HttpErrorResponse && erreur.error && typeof erreur.error === 'object') {
    return erreur.error as ErreurApi;
  }
  return undefined;
}
