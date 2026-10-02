import { HttpErrorResponse } from '@angular/common/http';

/**
 * Message lisible à partir d'une erreur HTTP du backend.
 * GlobalHandlerException renvoie { status, error, message, path } : on affiche "message".
 */
export function errorMessage(error: unknown, fallback = 'Une erreur est survenue. Réessayez.'): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Serveur injoignable. Vérifiez que le backend est démarré.';
    }
    const body = error.error;
    if (body && typeof body === 'object' && typeof body.message === 'string' && body.message) {
      return body.message;
    }
    if (typeof body === 'string' && body) {
      return body;
    }
  }
  return fallback;
}
