import { Injectable, effect, signal } from '@angular/core';

/**
 * Thème clair / sombre, partagé par toute l'application (vitrine, back-office, dashboard).
 *
 * - Le choix est mémorisé dans le localStorage (clé "drivehub_theme", déjà utilisée par le back-office).
 * - Sans choix mémorisé, on suit la préférence du système (comme next-themes dans l'ancienne landing).
 * - Le thème s'applique via la classe "dark" sur <html> (variante dark: de Tailwind et variables CSS).
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'drivehub_theme';

  readonly isDark = signal<boolean>(this.initialValue());

  constructor() {
    effect(() => document.documentElement.classList.toggle('dark', this.isDark()));
  }

  toggle(): void {
    this.isDark.update((dark) => !dark);
    try {
      localStorage.setItem(this.STORAGE_KEY, this.isDark() ? 'dark' : 'light');
    } catch {
      // stockage indisponible (navigation privée stricte) : le thème reste valable pour la session
    }
  }

  private initialValue(): boolean {
    try {
      const saved = localStorage.getItem(this.STORAGE_KEY);
      if (saved) {
        return saved === 'dark';
      }
    } catch {
      // ignore
    }
    return typeof window !== 'undefined' && window.matchMedia?.('(prefers-color-scheme: dark)').matches;
  }
}
