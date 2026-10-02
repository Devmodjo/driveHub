import { Component, input } from '@angular/core';

/** Cadre commun des pages de connexion / inscription (même fond quadrillé que la vitrine). */
@Component({
  selector: 'app-auth-shell',
  template: `
    <section class="relative min-h-screen pt-36 pb-24 bg-[#fafafa] dark:bg-[#080808] hub-grid">
      <div class="container mx-auto px-6 flex justify-center">
        <div class="premium-card rounded-[32px] w-full p-8 md:p-12" [class]="wide() ? 'max-w-3xl' : 'max-w-md'">
          <h2 class="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-4">{{ badge() }}</h2>
          <h1 class="font-display text-3xl md:text-4xl font-black text-black dark:text-white mb-3 tracking-tight">{{ title() }}</h1>
          @if (subtitle()) {
            <p class="text-black/50 dark:text-white/50 font-light mb-8">{{ subtitle() }}</p>
          }
          <ng-content />
        </div>
      </div>
    </section>
  `,
})
export class AuthShellComponent {
  readonly badge = input('DriveHub');
  readonly title = input.required<string>();
  readonly subtitle = input('');
  readonly wide = input(false);
}
