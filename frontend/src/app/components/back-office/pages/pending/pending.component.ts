import { Component } from '@angular/core';

/**
 * Page "Demandes en attente".
 * Route : /dashboard/pending
 * Contiendra la liste des admins et auto-écoles en attente de validation.
 */
@Component({
  selector: 'app-pending',
  imports: [],
  template: `
    <div class="page-header">
      <h2 class="page-title">Demandes en attente</h2>
      <p class="page-subtitle">Admins et auto-écoles en attente de validation</p>
    </div>
    <div class="placeholder-content">
      <p>Les demandes en attente seront affichées ici.</p>
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
export class PendingComponent {}
