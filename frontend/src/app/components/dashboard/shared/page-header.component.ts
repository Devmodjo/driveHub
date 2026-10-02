import { Component, input } from '@angular/core';

/** Titre de page du dashboard, avec un emplacement pour les boutons d'action. */
@Component({
  selector: 'app-page-header',
  template: `
    <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-4 mb-8">
      <div>
        <h2 class="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-2">{{ badge() }}</h2>
        <h1 class="font-display text-3xl md:text-4xl font-black text-black dark:text-white tracking-tight">{{ title() }}</h1>
        @if (subtitle()) { <p class="text-black/50 dark:text-white/50 font-light mt-2">{{ subtitle() }}</p> }
      </div>
      <div class="flex gap-3 flex-wrap"><ng-content /></div>
    </div>
  `,
})
export class PageHeaderComponent {
  readonly badge = input('Espace auto-école');
  readonly title = input.required<string>();
  readonly subtitle = input('');
}
