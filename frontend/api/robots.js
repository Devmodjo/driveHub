/**
 * Règles pour les moteurs de recherche : https://<site>/robots.txt (réécrit vers cette fonction dans vercel.json).
 * Les pages publiques sont indexées ; les espaces privés (tableau de bord, back-office, connexion) ne le sont pas.
 */
module.exports = function handler(req, res) {
  const host = req.headers['x-forwarded-host'] || req.headers.host;
  res.setHeader('Content-Type', 'text/plain; charset=utf-8');
  res.setHeader('Cache-Control', 'public, s-maxage=86400');
  res.status(200).send([
    'User-agent: *',
    'Allow: /',
    'Disallow: /dashboard',
    'Disallow: /backoffice',
    'Disallow: /connexion',
    'Disallow: /inscription',
    'Disallow: /verify-email',
    'Disallow: /reset-password',
    'Disallow: /invitation',
    '',
    `Sitemap: https://${host}/sitemap.xml`,
    '',
  ].join('\n'));
};
