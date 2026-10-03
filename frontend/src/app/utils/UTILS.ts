import { environment } from '../../environments/environment';

/**
 * Adresses de l'API. La racine vient de src/environments :
 *  - npm start (développement) : http://localhost:8082/api/
 *  - ng build (production, Vercel) : apiUrl de environment.ts
 */

/** Racine de l'API pour l'espace auto-école / élève (routes /api/auth, /api/students, /api/payments...). */
export const API_URL = environment.apiUrl;
/** Back-office : API des administrateurs de la plateforme. */
export const BASE_URL = `${environment.apiUrl}platform/`;
export const SCHOOL_URL = `${environment.apiUrl}driving-schools/`;

/** Délai de validation annoncé par l'équipe DriveHub (même valeur que ValidationDelay côté backend). */
export const VALIDATION_DELAY = '48 à 72 heures';
