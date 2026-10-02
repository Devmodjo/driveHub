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

/**
 * Message lisible à afficher dans l'alerte d'un formulaire. Ordre de priorité :
 *  1. le message précis du backend (ex : « Ce nom d'auto-école est déjà utilisé ») ;
 *  2. le message fourni par la page (`fallback`), qui connaît le contexte
 *     (ex : sur une page de connexion, un 401 signifie « Email ou mot de passe incorrect ») ;
 *  3. un message général selon le code HTTP.
 * Exception : serveur injoignable (code 0), message toujours le même.
 */
export function errorMessage(error: unknown, fallback?: string): string {
  const generic = 'Une erreur est survenue. Réessayez.';
  if (!(error instanceof HttpErrorResponse)) {
    return fallback ?? generic;
  }
  if (error.status === 0) {
    return 'Serveur injoignable : vérifiez votre connexion Internet, puis réessayez.';
  }
  const body = error.error;
  const backendMessage =
    body && typeof body === 'object' && typeof body.message === 'string' && body.message ? body.message : null;

  // « Authentification requise » est le message générique du backend pour un jeton absent ou expiré
  if (backendMessage && backendMessage !== 'Authentification requise') {
    return backendMessage;
  }
  if (typeof body === 'string' && body) {
    return body;
  }
  if (fallback) {
    return fallback;
  }
  switch (error.status) {
    case 401:
      return 'Votre session a expiré : reconnectez-vous.';
    case 403:
      return "Vous n'avez pas les droits pour effectuer cette action.";
    case 404:
      return "L'élément demandé n'existe pas ou a été supprimé.";
    case 413:
      return 'Le contenu envoyé est trop volumineux.';
    case 429:
      return 'Trop de tentatives : patientez quelques instants avant de réessayer.';
  }
  if (error.status >= 500) {
    return 'Le serveur a rencontré un problème. Réessayez dans quelques instants ; si le problème persiste, contactez le support.';
  }
  return generic;
}

/**
 * Messages d'erreur par champ, renvoyés par le backend sous la forme { fieldErrors: { email: "...", ... } }.
 * Vide si l'erreur ne concerne pas un champ précis.
 *
 * Utilisation dans un composant :
 *   this.fieldErrors.set(fieldErrorsOf(err));
 *   @if (fieldErrors()['email']; as msg) { <p class="field-error">{{ msg }}</p> }
 */
export function fieldErrorsOf(error: unknown): Record<string, string> {
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
