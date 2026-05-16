import { Component } from '@angular/core';

/**
 * Page "Vue d'ensemble" du dashboard.
 * Route : /dashboard/overview
 * Contiendra les KPI cards une fois les données disponibles.
 */
@Component({
  selector: 'app-overview',
  imports: [],
  template: `
    <div class="page-header">
      <h2 class="page-title">Vue d'ensemble</h2>
      <p class="page-subtitle">Bienvenue sur le tableau de bord DriveHub</p>
    </div>
    <div class="placeholder-content">
      <p>Les statistiques seront affichées ici.</p>
    </div>
  `,
  styles: [`
    .page-header { margin-bottom: 28px; }
    .page-title { font-size: 1.3rem; font-weight: 600; color: var(--text-primary); }
    .page-subtitle { font-size: 0.85rem; color: var(--text-muted); margin-top: 4px; }
    .placeholder-content {
      background: var(--card-bg);
      border: 1px dashed var(--border-color);
      padding: 40px;
      text-align: center;
      color: var(--text-muted);
      font-size: 0.88rem;
    }
  `]
})
export class OverviewComponent {}
