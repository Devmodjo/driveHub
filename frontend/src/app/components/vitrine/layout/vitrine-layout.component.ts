import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FooterComponent } from './footer/footer.component';
import { NavbarComponent } from './navbar/navbar.component';

/** Gabarit de la vitrine : barre de navigation fixe, page, pied de page (équivalent de layout.tsx). */
@Component({
  selector: 'app-vitrine-layout',
  imports: [RouterOutlet, NavbarComponent, FooterComponent],
  template: `
    <div class="min-h-screen flex flex-col bg-background text-foreground font-sans antialiased transition-colors duration-300">
      <app-navbar />
      <main class="grow">
        <router-outlet />
      </main>
      <app-footer />
    </div>
  `,
})
export class VitrineLayoutComponent {}
