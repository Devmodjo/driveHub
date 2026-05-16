import { Component } from '@angular/core';

/**
 * Page "Gestion des admins".
 * Route : /dashboard/admins
 * Contiendra le tableau des admins avec les actions CRUD.
 */
@Component({
  selector: 'app-admins',
  imports: [],
  template: `
    <div class="page-header">
      <h2 class="page-title">Gestion des Admins</h2>
      <p class="page-subtitle">Liste et gestion de tous les administrateurs</p>
    </div>
    <div class="placeholder-content">
      <p>Le tableau des admins sera affiché ici.</p>
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
export class AdminsComponent {}
