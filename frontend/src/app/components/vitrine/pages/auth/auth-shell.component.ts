import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { ThemeService } from '../../../../services/theme-service/theme.service';
import { ICONS } from '../../../../shared/icons';

/**
 * Cadre commun des pages de connexion, d'inscription et de mot de passe : écran divisé en deux.
 *
 *  - à gauche (écrans larges uniquement) : la photo hero-bg-1, avec le même voile sombre que le haut
 *    de la page d'accueil, et le message de DriveHub ;
 *  - à droite : le logo, puis le formulaire de la page, inséré à la place de <ng-content />.
 *
 * Sur mobile, la photo est masquée : seul le formulaire s'affiche, sur toute la largeur.
 *
 * Utilisation :
 *   <app-auth-shell badge="Espace auto-école" title="Connexion" subtitle="...">
 *     <form>...</form>
 *   </app-auth-shell>
 */
@Component({
  selector: 'app-auth-shell',
  imports: [RouterLink, LucideDynamicIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="min-h-screen grid lg:grid-cols-2 bg-background text-foreground font-sans antialiased">
      <!-- Moitié gauche : photo (masquée sur mobile) -->
      <aside class="relative hidden lg:flex flex-col justify-end overflow-hidden p-12 xl:p-16">
        <!-- Versions WebP légères (npm run images) ; la photo occupe la moitié de l'écran (sizes="50vw") -->
        <img src="/images/optimized/hero-bg-1-960.webp"
             srcset="/images/optimized/hero-bg-1-480.webp 480w, /images/optimized/hero-bg-1-960.webp 960w,
                     /images/optimized/hero-bg-1-1440.webp 1440w"
             sizes="50vw" width="960" height="1440" fetchpriority="high" decoding="async"
             alt="" class="absolute inset-0 w-full h-full object-cover" />
        <div class="absolute inset-0 bg-gradient-to-t from-black via-black/60 to-black/10"></div>

        <div class="relative z-10 max-w-lg">
          <div class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white/10 border border-white/20 text-[10px] font-black uppercase tracking-[0.2em] text-white/90 mb-8 backdrop-blur-md">
            Digitalisation Auto-École
          </div>
          <p class="font-display text-4xl xl:text-5xl font-black tracking-tighter text-white leading-[1.05] mb-6">
            Moins de paperasse.<br />Plus de conduite.
          </p>
          <p class="text-lg text-white/70 font-light leading-relaxed">
            Inscriptions, plannings, paiements Mobile Money et suivi des examens :
            toute la vie de l'auto-école au même endroit.
          </p>
        </div>
      </aside>

      <!-- Moitié droite : formulaire -->
      <main class="flex flex-col min-h-screen px-6 py-8 sm:px-12">
        <div class="flex items-center justify-between">
          <!-- Logo : identique à celui de la barre de navigation de la vitrine -->
          <a routerLink="/" class="flex items-center gap-3 group transition-transform hover:scale-[1.02] active:scale-95">
            <div class="relative w-12 h-12 rounded-xl overflow-hidden shadow-lg shadow-[#0070f3]/20 border border-black/5 dark:border-white/10 bg-black">
              <img src="/dh_icon.png" alt="DH Icon" width="48" height="48" class="absolute inset-0 w-full h-full object-cover" />
            </div>
            <div class="flex flex-col">
              <span class="text-2xl font-black italic tracking-tighter leading-none">
                <span class="text-[#0070f3]">Drive</span>
                <span class="text-black dark:text-white" style="-webkit-text-stroke: 1px currentColor; color: transparent">Hub</span>
              </span>
              <span class="text-[10px] font-bold uppercase tracking-[0.2em] text-[#0070f3]/80 leading-none">Management Hub</span>
            </div>
          </a>
          <div class="flex items-center gap-2">
            <a routerLink="/" class="hidden sm:inline-flex text-sm font-medium text-black/60 dark:text-white/60 hover:text-[#0070f3] transition-colors px-3 py-2">
              Retour à l'accueil
            </a>
            <button type="button" (click)="theme.toggle()" aria-label="Changer de thème"
                    class="p-2 rounded-lg bg-black/5 dark:bg-white/5 hover:bg-black/10 dark:hover:bg-white/10 transition-colors text-black dark:text-white">
              <svg [lucideIcon]="theme.isDark() ? icons.Sun : icons.Moon" [size]="20" />
            </button>
          </div>
        </div>

        <div class="grow flex items-center justify-center py-12">
          <div class="w-full" [class]="wide() ? 'max-w-xl' : 'max-w-md'">
            <h2 class="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-4">{{ badge() }}</h2>
            <h1 class="font-display text-3xl md:text-4xl font-black text-black dark:text-white mb-3 tracking-tight">{{ title() }}</h1>
            @if (subtitle()) {
              <p class="text-black/50 dark:text-white/50 font-light mb-8">{{ subtitle() }}</p>
            } @else {
              <div class="mb-8"></div>
            }
            <ng-content />
          </div>
        </div>

        <p class="text-xs text-black/40 dark:text-white/40 text-center">© DriveHub · Plateforme de gestion d'auto-école</p>
      </main>
    </div>
  `,
})
export class AuthShellComponent {
  protected readonly icons = ICONS;
  protected readonly theme = inject(ThemeService);

  readonly badge = input('DriveHub');
  readonly title = input.required<string>();
  readonly subtitle = input('');
  /** Formulaire plus large (inscription : deux colonnes de champs). */
  readonly wide = input(false);
}
