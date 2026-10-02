import { Injectable, computed, signal } from '@angular/core';
import en from './en.json';
import fr from './fr.json';

/** Structure des traductions (déduite de fr.json : ajouter une clé dans fr.json ET en.json). */
export type Dictionary = typeof fr;
export type Locale = 'fr' | 'en';

const DICTIONARIES: Record<Locale, Dictionary> = { fr, en };
const STORAGE_KEY = 'drivehub_locale';

/**
 * Traductions de la vitrine (remplace DictionaryProvider + cookie NEXT_LOCALE de l'ancienne landing).
 * Le changement de langue est instantané (signal) et mémorisé dans le localStorage.
 */
@Injectable({ providedIn: 'root' })
export class I18nService {
  readonly locale = signal<Locale>(this.initialLocale());
  readonly dict = computed<Dictionary>(() => DICTIONARIES[this.locale()]);

  toggleLocale(): void {
    this.setLocale(this.locale() === 'fr' ? 'en' : 'fr');
  }

  setLocale(locale: Locale): void {
    this.locale.set(locale);
    document.documentElement.lang = locale;
    try {
      localStorage.setItem(STORAGE_KEY, locale);
    } catch {
      // ignore
    }
  }

  private initialLocale(): Locale {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      return saved === 'en' ? 'en' : 'fr';
    } catch {
      return 'fr';
    }
  }
}
