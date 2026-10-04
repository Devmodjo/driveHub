/**
 * Adresse de l'API backend pour les fonctions Vercel (même variable API_URL que pour le build).
 * « https://x.onrender.com », « .../ » ou « .../api » donnent tous « https://x.onrender.com/api ».
 */
module.exports = function apiBase() {
  const raw = (process.env.API_URL || '').trim().replace(/\/+$/, '').replace(/\/api$/, '');
  return raw ? raw + '/api' : null;
};
