import { DOCUMENT } from '@angular/common';
import { Injectable, inject } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';

/** Ce que Google et les réseaux sociaux affichent pour une page. */
export interface SeoPage {
  /** Titre de l'onglet et du résultat Google (60 caractères environ) */
  title: string;
  /** Résumé affiché sous le titre dans Google (155 caractères environ) */
  description: string;
  /** Chemin de la page, ex : /auto-ecoles/auto-ecole-le-volant-douala */
  path: string;
  /** Image de partage (WhatsApp, Facebook) ; par défaut le logo DriveHub */
  image?: string;
}

/**
 * Référencement (SEO) des pages publiques : titre, description, adresse canonique, aperçu de partage
 * (Open Graph, utilisé par WhatsApp et Facebook) et données structurées schema.org (JSON-LD) qui
 * aident Google à comprendre qu'une page décrit une auto-école (nom, ville, téléphone...).
 *
 * Google exécute le JavaScript des pages : ces balises sont lues même si elles sont ajoutées par Angular.
 * Le plan du site (/sitemap.xml) liste toutes les pages d'auto-écoles (voir frontend/api/sitemap.js).
 */
@Injectable({ providedIn: 'root' })
export class SeoService {
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);
  private readonly document = inject(DOCUMENT);

  setPage(page: SeoPage): void {
    const origin = this.document.location?.origin ?? '';
    const url = origin + page.path;
    const image = page.image ?? origin + '/dh_icon.png';
    const description = page.description.length > 160 ? page.description.slice(0, 157).trimEnd() + '...' : page.description;

    this.title.setTitle(page.title);
    this.meta.updateTag({ name: 'description', content: description });
    this.meta.updateTag({ property: 'og:type', content: 'website' });
    this.meta.updateTag({ property: 'og:site_name', content: 'DriveHub' });
    this.meta.updateTag({ property: 'og:locale', content: 'fr_FR' });
    this.meta.updateTag({ property: 'og:title', content: page.title });
    this.meta.updateTag({ property: 'og:description', content: description });
    this.meta.updateTag({ property: 'og:url', content: url });
    this.meta.updateTag({ property: 'og:image', content: image });
    this.meta.updateTag({ name: 'twitter:card', content: 'summary' });
    this.setCanonical(url);
  }

  /** Données structurées schema.org (une seule à la fois ; null pour les retirer). */
  setStructuredData(data: object | null): void {
    const id = 'drivehub-structured-data';
    this.document.getElementById(id)?.remove();
    if (!data) return;
    const script = this.document.createElement('script');
    script.id = id;
    script.type = 'application/ld+json';
    script.text = JSON.stringify(data);
    this.document.head.appendChild(script);
  }

  /** Adresse officielle de la page (évite que Google compte deux fois la même page). */
  private setCanonical(url: string): void {
    let link = this.document.head.querySelector<HTMLLinkElement>('link[rel="canonical"]');
    if (!link) {
      link = this.document.createElement('link');
      link.rel = 'canonical';
      this.document.head.appendChild(link);
    }
    link.href = url;
  }
}
