/**
 * Configuration de PRODUCTION (utilisée par `ng build`, donc par Vercel).
 *
 * apiUrl : adresse publique du backend Spring Boot, terminée par /api/.
 * À modifier quand le backend est hébergé (Render, Railway, VPS...), puis redéployer le frontend.
 * Le domaine du frontend (ex : https://cmdrivehub.vercel.app) doit aussi être ajouté côté backend
 * dans CORS_ALLOWED_ORIGINS, et APP_FRONTEND_URL doit pointer vers lui (liens des emails).
 */
export const environment = {
  production: true,
  apiUrl: 'https://api.drivehub.cm/api/',
};
