/**
 * Optimisation des grandes images de la vitrine.
 *
 * Les photos d'origine (images-src/hero-bg-*.jpg) font plusieurs mégaoctets (jusqu'à 18 Mo, 7360 px de large) :
 * les télécharger ralentit fortement l'affichage. Ce script crée des versions WebP à plusieurs largeurs ;
 * le navigateur choisit la plus adaptée à l'écran grâce à l'attribut srcset de <img>.
 *
 * Utilisation (à relancer seulement si une photo d'origine change) :
 *   npm run images
 *
 * Résultat : public/images/optimized/<nom>-<largeur>.webp
 */
import sharp from 'sharp';
import { mkdirSync } from 'node:fs';

const SOURCE = 'images-src';          // photos d'origine (non publiées : trop lourdes)
const TARGET = 'public/images/optimized';

// largeurs générées pour chaque image (en pixels)
const IMAGES = {
  'hero-bg-2': [640, 1280, 1920, 2880],   // fond plein écran de l'accueil
  'hero-bg-1': [480, 960, 1440],          // moitié gauche des pages de connexion / inscription
};

mkdirSync(TARGET, { recursive: true });
for (const [name, widths] of Object.entries(IMAGES)) {
  for (const width of widths) {
    const out = `${TARGET}/${name}-${width}.webp`;
    const info = await sharp(`${SOURCE}/${name}.jpg`)
      .rotate()                          // respecte l'orientation EXIF de la photo
      .resize({ width, withoutEnlargement: true })
      .webp({ quality: 80 })
      .toFile(out);
    console.log(`${out}  ${info.width}x${info.height}  ${(info.size / 1024).toFixed(0)} Ko`);
  }
}
