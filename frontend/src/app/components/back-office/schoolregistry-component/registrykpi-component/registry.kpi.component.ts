import { Component, Input } from '@angular/core';

/**
 * Carte KPI reutilisable pour afficher une metrique cle.
 * Accepte un titre, une valeur principale, un sous-texte optionnel,
 * et une couleur d'accent appliquee a la bordure gauche.
 */
@Component({
  selector: 'registry-kpi',
  imports: [],
  template: `
    <div class="kpi-card" [style.borderLeftColor]="KpiColor">
      <span class="kpi-title">{{ KpiTitle }}</span>
      <span class="kpi-value" [style.color]="KpiColor">{{ KpiResult ?? '—' }}</span>
      @if (KpiSubtext) {
        <span class="kpi-subtext" [style.color]="KpiSubtextColor">{{ KpiSubtext }}</span>
      }
    </div>
  `,
  styleUrl: './registry.kpi.component.css',
})
export class RegistryKpiComponent {
  @Input() KpiTitle?: string;
  @Input() KpiResult?: number | string;
  @Input() KpiSubtext?: string;
  @Input() KpiColor: string = 'var(--accent)';
  @Input() KpiSubtextColor: string = 'var(--text-muted)';
}
