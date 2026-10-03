/**
 * Adresse de l'API choisie au moment du build (utilisé par Vercel et la CI).
 *
 * Si la variable d'environnement API_URL est définie (ex : https://drivehub-api.onrender.com),
 * elle remplace apiUrl dans src/environments/environment.ts avant `ng build`.
 * Sans API_URL, le fichier n'est pas modifié (valeur écrite dans le fichier).
 *
 * Sur Vercel : Project > Settings > Environment Variables > API_URL, puis redéployer.
 */
import { readFileSync, writeFileSync } from 'node:fs';

const raw = process.env.API_URL?.trim();
if (!raw) {
  console.log('[set-api-url] API_URL non définie : adresse de environment.ts conservée.');
  process.exit(0);
}
if (!/^https?:\/\/[^\s'"]+$/.test(raw)) {
  console.error(`[set-api-url] API_URL invalide : "${raw}" (attendu : https://mon-api.exemple.com)`);
  process.exit(1);
}

// On accepte "https://api.x.cm", "https://api.x.cm/" ou "https://api.x.cm/api" : le résultat finit toujours par /api/
const apiUrl = raw.replace(/\/+$/, '').replace(/\/api$/, '') + '/api/';
const file = new URL('../src/environments/environment.ts', import.meta.url);
const source = readFileSync(file, 'utf8');
const updated = source.replace(/apiUrl:\s*'[^']*'/, `apiUrl: '${apiUrl}'`);
if (updated === source && !source.includes(`apiUrl: '${apiUrl}'`)) {
  console.error('[set-api-url] apiUrl introuvable dans environment.ts');
  process.exit(1);
}
writeFileSync(file, updated);
console.log(`[set-api-url] API : ${apiUrl}`);
