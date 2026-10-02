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

/**
 * Erreurs par champ renvoyées par le backend, sous la forme { fieldErrors: { email: "...", ... } }.
 *
 * Le backend ne fournit pas toujours cette table : on renvoie alors un objet vide.
 * Utilisation dans un gabarit : @if (errors().email) { <p>{{ errors().email }}</p> }
 */
export function fieldErrors(error: unknown): Record<string, string> {
  if (!(error instanceof HttpErrorResponse)) {
    return {};
  }
  const raw = error.error?.fieldErrors;
  if (!raw || typeof raw !== 'object') {
    return {};
  }
  // On ne garde que les messages texte (sécurité si le backend renvoie autre chose)
  const result: Record<string, string> = {};
  for (const [field, message] of Object.entries(raw)) {
    if (typeof message === 'string' && message) {
      result[field] = message;
    }
  }
  return result;
}
