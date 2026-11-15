import { HttpErrorResponse } from '@angular/common/http';

import { codeErreur, erreursParChamp, messageErreur } from './message-erreur';

describe('messageErreur', () => {
  it('affiche le détail envoyé par le backend', () => {
    const erreur = new HttpErrorResponse({
      status: 409,
      error: { detail: 'Cet email appartient déjà à un compte', code: 'EMAIL_DEJA_UTILISE' },
    });
    expect(messageErreur(erreur)).toBe('Cet email appartient déjà à un compte');
    expect(codeErreur(erreur)).toBe('EMAIL_DEJA_UTILISE');
  });

  it('donne les messages par champ d\'une erreur DONNEES_INVALIDES', () => {
    const erreur = new HttpErrorResponse({
      status: 400,
      error: { code: 'DONNEES_INVALIDES', erreurs: { nom: 'Le nom est obligatoire' } },
    });
    expect(erreursParChamp(erreur)).toEqual({ nom: 'Le nom est obligatoire' });
  });

  it('explique que le serveur ne répond pas', () => {
    expect(messageErreur(new HttpErrorResponse({ status: 0 }))).toContain('Le serveur ne répond pas');
  });

  it('donne un message général pour une erreur inconnue', () => {
    expect(messageErreur(new Error('boum'))).toContain('Une erreur inattendue');
    expect(erreursParChamp(new Error('boum'))).toEqual({});
  });
});
