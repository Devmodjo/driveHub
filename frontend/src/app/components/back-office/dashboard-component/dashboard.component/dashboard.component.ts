import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DashboardNavigationComponent } from '../dashboard-navigation/dashboard.navigation.component';

/**
 * DashboardComponent — composant "coque" du dashboard.
 *
 * Il ne fait qu'assembler la sidebar et le contenu principal.
 * Le contenu change via le <router-outlet> selon la route active :
 *   /dashboard/overview → OverviewComponent
 *   /dashboard/admins   → AdminsComponent
 *   /dashboard/pending  → PendingComponent
 */
@Component({
  selector: 'app-dashboard',
  imports: [RouterOutlet, DashboardNavigationComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent {}
