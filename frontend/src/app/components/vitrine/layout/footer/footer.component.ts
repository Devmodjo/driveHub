import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { ICONS } from '../../../../shared/icons';
import { I18nService } from '../../i18n/i18n.service';

/** Pied de page de la vitrine (conversion de Footer.tsx). */
@Component({
  selector: 'app-footer',
  imports: [RouterLink, LucideAngularModule],
  templateUrl: './footer.component.html',
})
export class FooterComponent {
  protected readonly icons = ICONS;
  protected readonly i18n = inject(I18nService);
  protected readonly year = new Date().getFullYear();
}
