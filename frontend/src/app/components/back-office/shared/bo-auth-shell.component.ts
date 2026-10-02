import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { ThemeService } from '../../../services/theme-service/theme.service';
import { ICONS } from '../../../shared/icons';

/**
 * Cadre des pages de connexion et d'inscription du back-office : écran divisé en deux,
 * comme les pages de connexion des auto-écoles (même photo, même logo), mais avec le message
 * « Administration DriveHub ».
 *
 * - Mobile : la photo est masquée, le formulaire occupe toute la largeur.
 * - À partir de lg: : photo à gauche, formulaire à droite.
 *
 * Utilisation :
 *   <bo-auth-shell title="Connexion" subtitle="..."> <form>...</form> </bo-auth-shell>
 */
@Component({
  selector: 'bo-auth-shell',
  imports: [RouterLink, LucideDynamicIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="min-h-screen grid lg:grid-cols-2 bg-background text-foreground font-sans antialiased">
      <!-- Moitié gauche : photo (écrans larges uniquement) -->
      <aside class="relative hidden lg:flex flex-col justify-end overflow-hidden p-12 xl:p-16">
        <img src="/images/optimized/hero-bg-1-960.webp"
             srcset="/images/optimized/hero-bg-1-480.webp 480w, /images/optimized/hero-bg-1-960.webp 960w,
                     /images/optimized/hero-bg-1-1440.webp 1440w"
             sizes="50vw" width="960" height="1440" fetchpriority="high" decoding="async"
             alt="" class="absolute inset-0 w-full h-full object-cover" />
        <!-- Voile sombre pour garantir la lisibilité du texte blanc sur la photo -->
        <div class="absolute inset-0 bg-black/65"></div>

        <div class="relative z-10 max-w-lg">
          <div class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white/10 border border-white/20 text-[10px] font-black uppercase tracking-[0.2em] text-white/90 mb-8 backdrop-blur-md">
            <svg [lucideIcon]="icons.ShieldCheck" [size]="14" /> Espace réservé
          </div>
          <p class="font-display text-4xl xl:text-5xl font-black tracking-tighter text-white leading-[1.05] mb-6">
            Administration DriveHub
          </p>
          <p class="text-lg text-white/70 font-light leading-relaxed">
            Validez les auto-écoles, gérez l'équipe de la plateforme et communiquez
            avec les auto-écoles, les moniteurs et les élèves.
          </p>
        </div>
      </aside>

      <!-- Moitié droite : formulaire -->
      <main class="flex flex-col min-h-screen px-4 py-5 sm:px-12 sm:py-8">
        <div class="flex items-center justify-between gap-3">
          <a routerLink="/" class="flex items-center gap-3 min-w-0">
            <div class="relative w-11 h-11 shrink-0 rounded-xl overflow-hidden shadow-lg shadow-[#0070f3]/20 border border-black/5 dark:border-white/10 bg-black">
              <img src="/dh_icon.png" alt="" width="44" height="44" class="absolute inset-0 w-full h-full object-cover" />
            </div>
            <div class="flex flex-col min-w-0">
              <span class="text-xl font-black italic tracking-tighter leading-none"><span class="text-[#0070f3]">Drive</span>Hub</span>
              <span class="text-[10px] font-bold uppercase tracking-[0.2em] text-[#0070f3]/80 leading-none mt-1">Back-office</span>
            </div>
          </a>
          <button type="button" class="bo-icon-btn bg-black/5 dark:bg-white/5" (click)="theme.toggle()" aria-label="Changer de thème">
            <svg [lucideIcon]="theme.isDark() ? icons.Sun : icons.Moon" [size]="20" />
          </button>
        </div>

        <div class="grow flex items-center justify-center py-8 sm:py-12">
          <div class="w-full" [class]="wide() ? 'max-w-xl' : 'max-w-md'">
            <p class="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-3">Administration DriveHub</p>
            <h1 class="font-display text-3xl md:text-4xl font-black text-black dark:text-white mb-2 tracking-tight">{{ title() }}</h1>
            <p class="text-black/50 dark:text-white/50 font-light mb-8">{{ subtitle() }}</p>
            <ng-content />
          </div>
        </div>

        <p class="text-xs text-black/40 dark:text-white/40 text-center">DriveHub · Console d'administration de la plateforme</p>
      </main>
    </div>
  `,
})
export class BoAuthShellComponent {
  protected readonly icons = ICONS;
  protected readonly theme = inject(ThemeService);

  readonly title = input.required<string>();
  readonly subtitle = input('');
  /** Formulaire plus large (inscription : deux colonnes de champs à partir de sm:). */
  readonly wide = input(false);
}
