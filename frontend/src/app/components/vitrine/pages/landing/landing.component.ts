import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { ICONS } from '../../../../shared/icons';
import { I18nService } from '../../i18n/i18n.service';
import { AboutComponent } from '../../sections/about.component';
import { FaqComponent } from '../../sections/faq.component';
import { FeaturesComponent } from '../../sections/features.component';
import { HeroComponent } from '../../sections/hero.component';
import { HowItWorksComponent } from '../../sections/how-it-works.component';
import { PricingComponent } from '../../sections/pricing.component';

/** Page d'accueil (conversion de app/page.tsx). La section Témoignages reste désactivée, comme dans l'original. */
@Component({
  selector: 'app-landing',
  imports: [
    RouterLink, LucideAngularModule, HeroComponent, HowItWorksComponent, FeaturesComponent, AboutComponent,
    PricingComponent, FaqComponent,
  ],
  template: `
    <div class="flex flex-col">
      <app-hero />
      <app-how-it-works />
      <app-features />
      <app-about />
      <app-pricing />
      <app-faq />

      <!-- Appel à l'action -->
      <section class="py-32 bg-white dark:bg-black border-t border-black/5 dark:border-white/5 relative overflow-hidden">
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-2xl h-[300px] bg-[#0070f3]/5 dark:bg-[#0070f3]/10 blur-[130px] rounded-full pointer-events-none"></div>

        <div class="container mx-auto px-6 text-center relative z-10">
          <h2 class="font-display text-4xl md:text-5xl lg:text-6xl font-black text-black dark:text-white mb-8 tracking-tight">
            {{ i18n.dict().CTA.title }}
          </h2>
          <p class="text-black/60 dark:text-white/60 text-xl mb-12 max-w-2xl mx-auto font-light leading-relaxed">
            {{ i18n.dict().CTA.description }}
          </p>
          <div class="flex flex-col sm:flex-row gap-4 justify-center items-center">
            <a routerLink="/inscription"
               class="w-full sm:w-auto bg-[#0070f3] text-white hover:bg-[#0070f3]/90 px-8 py-4 rounded-full font-bold text-lg transition-all shadow-lg shadow-[#0070f3]/25 flex items-center justify-center gap-2">
              {{ i18n.dict().CTA.btn1 }} <lucide-icon [img]="icons.ArrowRight" [size]="20" />
            </a>
            <a routerLink="/auto-ecoles"
               class="w-full sm:w-auto bg-black/5 dark:bg-white/5 text-black dark:text-white border border-black/5 dark:border-white/5 hover:bg-black/10 dark:hover:bg-white/10 px-8 py-4 rounded-full font-medium text-lg transition-all backdrop-blur-sm">
              {{ i18n.dict().CTA.btn2 }}
            </a>
          </div>
        </div>
      </section>
    </div>
  `,
})
export class LandingComponent {
  protected readonly icons = ICONS;
  protected readonly i18n = inject(I18nService);
}
