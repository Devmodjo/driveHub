/**
 * Plan du site pour Google : https://<site>/sitemap.xml (réécrit vers cette fonction dans vercel.json).
 *
 * Liste les pages publiques et la page de CHAQUE auto-école validée, lue en direct dans l'API :
 * une nouvelle auto-école apparaît dans le plan sans redéployer le site.
 * À déclarer une fois dans Google Search Console (voir docs/DEPLOIEMENT.md).
 */
const apiBase = require('./_api-url');

const escapeXml = (s) => String(s).replace(/[<>&'"]/g, (c) => ({ '<': '&lt;', '>': '&gt;', '&': '&amp;', "'": '&apos;', '"': '&quot;' })[c]);

module.exports = async function handler(req, res) {
  const host = req.headers['x-forwarded-host'] || req.headers.host;
  const origin = `https://${host}`;
  const urls = [
    { loc: '/', priority: '1.0' },
    { loc: '/auto-ecoles', priority: '0.9' },
    { loc: '/confidentialite', priority: '0.3' },
  ];
  const api = apiBase();
  if (api) {
    try {
      const response = await fetch(`${api}/driving-schools/public/all`);
      if (response.ok) {
        for (const school of await response.json()) {
          if (school.slug) {
            urls.push({ loc: `/auto-ecoles/${encodeURIComponent(school.slug)}`, lastmod: school.createdAt, priority: '0.8' });
          }
        }
      }
    } catch (e) {
      // API indisponible : on renvoie au moins les pages fixes
      console.error('sitemap : API injoignable', e.message);
    }
  }
  const body = urls.map((u) =>
    `  <url><loc>${escapeXml(origin + u.loc)}</loc>${u.lastmod ? `<lastmod>${escapeXml(u.lastmod)}</lastmod>` : ''}<priority>${u.priority}</priority></url>`).join('\n');
  res.setHeader('Content-Type', 'application/xml; charset=utf-8');
  res.setHeader('Cache-Control', 'public, s-maxage=3600, stale-while-revalidate=86400');   // recalculé au plus une fois par heure
  res.status(200).send(`<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${body}\n</urlset>\n`);
};
