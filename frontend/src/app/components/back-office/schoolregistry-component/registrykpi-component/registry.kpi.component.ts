import { Component, Input } from '@angular/core';

/**
 * Carte KPI de la page « Auto-écoles » : un titre, une valeur principale, un sous-texte facultatif
 * et une couleur d'accent (pastille à gauche du titre et couleur du chiffre).
 */
@Component({
  selector: 'registry-kpi',
  template: `
    <div class="bo-card h-full p-4 sm:p-5 flex flex-col gap-1.5">
      <p class="flex items-center gap-2 text-[11px] sm:text-xs font-bold uppercase tracking-[0.12em] text-black/50 dark:text-white/50 leading-snug">
        <span class="w-2 h-2 shrink-0 rounded-full" [style.backgroundColor]="KpiColor"></span>
        {{ KpiTitle }}
      </p>
      <p class="font-display text-2xl sm:text-3xl font-black tracking-tight" [style.color]="KpiColor">{{ KpiResult ?? '—' }}</p>
      @if (KpiSubtext) {
        <p class="text-xs font-semibold text-black/50 dark:text-white/50" [style.color]="KpiSubtextColor">{{ KpiSubtext }}</p>
      }
    </div>
  `,
})
export class RegistryKpiComponent {
  @Input() KpiTitle?: string;
  @Input() KpiResult?: number | string;
  @Input() KpiSubtext?: string;
  @Input() KpiColor: string = '#0070f3';
  /** Couleur du sous-texte ; vide = couleur atténuée par défaut (lisible en clair et en sombre). */
  @Input() KpiSubtextColor: string | null = null;
}
