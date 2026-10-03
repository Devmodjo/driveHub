import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage, fieldErrorsOf } from './http-error';

describe('http-error', () => {
  it('errorMessage lit le message du backend, sinon le message par défaut', () => {
    const withMessage = new HttpErrorResponse({ status: 400, error: { message: 'Objet trop court' } });
    expect(errorMessage(withMessage, 'défaut')).toBe('Objet trop court');
    expect(errorMessage(new HttpErrorResponse({ status: 500, error: null }), 'défaut')).toBe('défaut');
  });

  it('errorMessage : serveur injoignable, puis message général selon le code HTTP', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 0 }), 'défaut')).toContain('Serveur injoignable');
    expect(errorMessage(new HttpErrorResponse({ status: 401, error: { message: 'Authentification requise' } })))
      .toBe('Votre session a expiré : reconnectez-vous.');
    expect(errorMessage(new HttpErrorResponse({ status: 403, error: null }))).toContain('droits');
    expect(errorMessage(new HttpErrorResponse({ status: 503, error: null }))).toContain('Le serveur a rencontré un problème');
  });

  it('fieldErrors renvoie la table des erreurs par champ, ou un objet vide', () => {
    const err = new HttpErrorResponse({ status: 400, error: { message: 'x', fieldErrors: { subject: 'Trop court', bad: 3 } } });
    expect(fieldErrorsOf(err)).toEqual({ subject: 'Trop court' });
    expect(fieldErrorsOf(new HttpErrorResponse({ status: 400, error: { message: 'x' } }))).toEqual({});
    expect(fieldErrorsOf(new Error('autre'))).toEqual({});
  });
});
