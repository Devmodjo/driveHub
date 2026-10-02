import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage, fieldErrors } from './http-error';

describe('http-error', () => {
  it('errorMessage lit le message du backend, sinon le message par défaut', () => {
    const withMessage = new HttpErrorResponse({ status: 400, error: { message: 'Objet trop court' } });
    expect(errorMessage(withMessage, 'défaut')).toBe('Objet trop court');
    expect(errorMessage(new HttpErrorResponse({ status: 500, error: null }), 'défaut')).toBe('défaut');
  });

  it('fieldErrors renvoie la table des erreurs par champ, ou un objet vide', () => {
    const err = new HttpErrorResponse({ status: 400, error: { message: 'x', fieldErrors: { subject: 'Trop court', bad: 3 } } });
    expect(fieldErrors(err)).toEqual({ subject: 'Trop court' });
    expect(fieldErrors(new HttpErrorResponse({ status: 400, error: { message: 'x' } }))).toEqual({});
    expect(fieldErrors(new Error('autre'))).toEqual({});
  });
});
