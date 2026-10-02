import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { RevealDirective } from '../../../directives/reveal.directive';
import { ICONS } from '../../../shared/icons';
import { I18nService } from '../i18n/i18n.service';

/** Section « Tarifs » (conversion de Pricing.tsx). */
@Component({
  selector: 'app-pricing',
  imports: [RouterLink, LucideAngularModule, RevealDirective],
  template: `
    <section id="pricing" class="py-32 bg-white dark:bg-black border-y border-black/5 dark:border-white/5 relative overflow-hidden">
      <div class="container mx-auto px-6 relative z-10">
        <div class="text-center max-w-3xl mx-auto mb-24">
          <h2 appReveal class="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-6 block">{{ i18n.dict().Pricing.badge }}</h2>
          <h3 appReveal class="font-display text-4xl md:text-6xl font-black text-black dark:text-white mb-8 tracking-tight leading-[1.1]">{{ i18n.dict().Pricing.title }}</h3>
          <p appReveal class="text-xl md:text-2xl text-black/50 dark:text-white/50 font-light leading-relaxed">{{ i18n.dict().Pricing.description }}</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10 max-w-7xl mx-auto items-stretch">
          @for (plan of plans(); track plan.name) {
            <div appReveal [revealDelay]="$index * 100" [revealDuration]="600"
                 class="p-1 bg-gradient-to-b from-black/[0.03] to-transparent dark:from-white/[0.03] dark:to-transparent rounded-[40px] border border-black/5 dark:border-white/5 flex flex-col relative h-full hover:-translate-y-2"
                 [class]="plan.popular ? 'ring-2 ring-[#0070f3]/40' : ''">
              <div class="bg-white dark:bg-[#050505] p-12 rounded-[36px] h-full flex flex-col items-start">
                @if (plan.popular) {
                  <div class="absolute -top-5 left-1/2 -translate-x-1/2 px-8 py-2.5 rounded-full bg-[#0070f3] text-white text-[10px] font-black uppercase tracking-[0.2em] shadow-2xl shadow-[#0070f3]/40">
                    {{ i18n.dict().Pricing.popular }}
                  </div>
                }

                <div class="mb-10 w-full">
                  <h4 class="font-display text-3xl font-black text-black dark:text-white mb-4 tracking-tight">{{ plan.name }}</h4>
                  <p class="text-black/50 dark:text-white/50 text-base mb-10 leading-relaxed font-light">{{ plan.description }}</p>
                  <div class="flex items-baseline gap-2">
                    <span class="font-display text-6xl font-black text-black dark:text-white tracking-tighter">{{ plan.price }}</span>
                    <span class="text-black/30 dark:text-white/30 font-black text-[10px] uppercase tracking-widest">{{ plan.currency }}{{ plan.period }}</span>
                  </div>
                </div>

                <div class="h-px w-full bg-black/5 dark:bg-white/5 mb-10"></div>

                <ul class="space-y-6 mb-16 flex-grow w-full">
                  @for (feature of plan.features; track $index) {
                    <li class="flex items-start gap-4 text-base text-black/60 dark:text-white/60 font-light">
                      <lucide-icon [img]="icons.Check" [size]="22" [strokeWidth]="3" class="text-[#0070f3] shrink-0 mt-0.5" />
                      <span>{{ feature }}</span>
                    </li>
                  }
                </ul>

                <a routerLink="/inscription"
                   class="w-full py-5 rounded-2xl font-black transition-all text-[10px] uppercase tracking-[0.25em] flex items-center justify-center hover:scale-[1.02] active:scale-[0.98]"
                   [class]="plan.popular
                     ? 'bg-[#0070f3] text-white shadow-xl shadow-[#0070f3]/20 hover:shadow-[#0070f3]/40'
                     : 'bg-black/5 dark:bg-white/5 border border-black/5 dark:border-white/5 text-black dark:text-white hover:bg-black/10 dark:hover:bg-white/10'">
                  {{ plan.cta }}
                </a>
              </div>
            </div>
          }
        </div>
      </div>
    </section>
  `,
})
export class PricingComponent {
  protected readonly icons = ICONS;
  protected readonly i18n = inject(I18nService);

  protected readonly plans = computed(() => {
    const d = this.i18n.dict().Pricing;
    return [
      {
        name: d.plan1_name, price: '25.000', currency: 'FCFA', period: d.period, description: d.plan1_desc,
        features: [d.plan1_f1, d.plan1_f2, d.plan1_f3, d.plan1_f4], cta: d.plan1_cta, popular: false,
      },
      {
        name: d.plan2_name, price: '50.000', currency: 'FCFA', period: d.period, description: d.plan2_desc,
        features: [d.plan2_f1, d.plan2_f2, d.plan2_f3, d.plan2_f4, d.plan2_f5, d.plan2_f6], cta: d.plan2_cta, popular: true,
      },
      {
        name: d.plan3_name, price: d.plan3_price, currency: '', period: '', description: d.plan3_desc,
        features: [d.plan3_f1, d.plan3_f2, d.plan3_f3, d.plan3_f4], cta: d.plan3_cta, popular: false,
      },
    ];
  });
}
