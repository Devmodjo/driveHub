import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DashboardNavigationComponent } from '../dashboard-navigation/dashboard.navigation.component';

/**
 * DashboardComponent : composant « coque » du back-office.
 *
 * Il assemble la navigation et la zone de contenu. Le contenu change via le <router-outlet>
 * selon la route active (/backoffice/dashboard/overview, /schools, /pending, /admins, /emails).
 *
 * Mobile d'abord : marges de 16px sur téléphone, plus généreuses ensuite ; à partir de lg:,
 * le contenu est décalé de la largeur de la barre latérale fixe (18rem = w-72).
 */
@Component({
  selector: 'app-dashboard',
  imports: [RouterOutlet, DashboardNavigationComponent],
  template: `
    <div class="min-h-screen bg-[#fafafa] dark:bg-[#080808] text-black dark:text-white font-sans">
      <app-dashboard-navigation />
      <main class="lg:pl-72 min-w-0">
        <div class="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 md:py-8 lg:px-10 lg:py-10">
          <router-outlet />
        </div>
      </main>
    </div>
  `,
})
export class DashboardComponent {}
