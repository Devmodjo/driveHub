import { Component, Input } from '@angular/core';

/**
 * Carte d'indicateur (KPI) de la vue d'ensemble : un titre, un chiffre et une icône.
 * L'icône est passée entre les balises (<dashboard-kpi ...><svg ... /></dashboard-kpi>).
 *
 * Mobile : deux cartes par ligne (texte compact) ; la grille parente gère la disposition.
 */
@Component({
  selector: 'dashboard-kpi',
  template: `
    <div class="bo-card h-full p-4 sm:p-5 flex items-start justify-between gap-3">
      <div class="min-w-0">
        <p class="text-[11px] sm:text-xs font-bold uppercase tracking-[0.12em] text-black/50 dark:text-white/50 leading-snug">{{ KpiTitle }}</p>
        <p class="mt-2 font-display text-2xl sm:text-3xl font-black tracking-tight" [style.color]="KpiColor">{{ KpiResult ?? '0' }}</p>
      </div>
      <div class="hidden sm:flex w-11 h-11 shrink-0 items-center justify-center rounded-xl"
           [style.color]="KpiColor" [style.backgroundColor]="KpiBgColor">
        <ng-content></ng-content>
      </div>
    </div>
  `,
})
export class DashboardKpiComponent {
  @Input() KpiTitle?: string;
  @Input() KpiResult?: number | string;
  @Input() KpiColor?: string = 'currentColor';
  @Input() KpiBgColor?: string = 'transparent';
}
