import { HttpErrorResponse } from '@angular/common/http';

/**
 * Lecture des erreurs renvoyées par le backend.
 *
 * Le backend (GlobalHandlerException) répond toujours avec un corps de la forme :
 *   { status, error, message, path, fieldErrors? }
 *  - message     : phrase lisible, déjà précise (ex : « Ce nom d'auto-école est déjà utilisé ») ;
 *  - fieldErrors : seulement pour un formulaire invalide, le message de chaque champ
 *                  (ex : { description: "La présentation ne doit pas dépasser 2000 caractères" }).
 */

/** Message lisible à afficher dans l'alerte d'un formulaire. */
export function errorMessage(error: unknown, fallback = 'Une erreur est survenue. Réessayez.'): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }
  const body = error.error;
  const backendMessage =
    body && typeof body === 'object' && typeof body.message === 'string' && body.message ? body.message : null;

  switch (error.status) {
    case 0:
      return 'Serveur injoignable : vérifiez votre connexion Internet, puis réessayez.';
    case 401:
      return backendMessage && backendMessage !== 'Authentification requise'
        ? backendMessage
        : 'Votre session a expiré : reconnectez-vous.';
    case 403:
      return backendMessage ?? "Vous n'avez pas les droits pour effectuer cette action.";
    case 404:
      return backendMessage ?? "L'élément demandé n'existe pas ou a été supprimé.";
    case 413:
      return 'Le contenu envoyé est trop volumineux.';
    case 429:
      return 'Trop de tentatives : patientez quelques instants avant de réessayer.';
  }
  if (error.status >= 500) {
    return 'Le serveur a rencontré un problème. Réessayez dans quelques instants ; si le problème persiste, contactez le support.';
  }
  if (backendMessage) {
    return backendMessage;
  }
  if (typeof body === 'string' && body) {
    return body;
  }
  return fallback;
}

/**
 * Messages d'erreur par champ (vide si l'erreur ne concerne pas un champ précis).
 * Utilisation dans un composant :
 *   this.fieldErrors.set(fieldErrorsOf(err));
 *   @if (fieldErrors()['email']; as msg) { <p class="field-error">{{ msg }}</p> }
 */
export function fieldErrorsOf(error: unknown): Record<string, string> {
  if (error instanceof HttpErrorResponse && error.error && typeof error.error === 'object') {
    const fields = error.error.fieldErrors;
    if (fields && typeof fields === 'object') {
      return fields as Record<string, string>;
    }
  }
  return {};
}
