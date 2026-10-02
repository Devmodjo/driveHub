import { Component, HostListener, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { SessionService } from '../../../../services/session-service/session.service';
import { ThemeService } from '../../../../services/theme-service/theme.service';
import { ICONS } from '../../../../shared/icons';
import { I18nService } from '../../i18n/i18n.service';

/** Barre de navigation de la vitrine (conversion de Navbar.tsx). */
@Component({
  selector: 'app-navbar',
  imports: [RouterLink, LucideDynamicIcon],
  templateUrl: './navbar.component.html',
})
export class NavbarComponent {
  protected readonly icons = ICONS;
  protected readonly theme = inject(ThemeService);
  protected readonly i18n = inject(I18nService);
  protected readonly session = inject(SessionService);
  protected readonly isOpen = signal(false);
  protected readonly scrolled = signal(false);

  protected readonly navLinks = [
    { key: 'link_features', fragment: 'features' },
    { key: 'link_pricing', fragment: 'pricing' },
    { key: 'link_about', fragment: 'about' },
    { key: 'link_faq', fragment: 'faq' },
  ] as const;

  @HostListener('window:scroll')
  onScroll(): void {
    this.scrolled.set(window.scrollY > 20);
  }
}
