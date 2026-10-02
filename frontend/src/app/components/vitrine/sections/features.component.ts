import { Component, computed, inject } from '@angular/core';
import { LucideDynamicIcon } from '@lucide/angular';
import { RevealDirective } from '../../../directives/reveal.directive';
import { ICONS } from '../../../shared/icons';
import { I18nService } from '../i18n/i18n.service';

/** Section « Fonctionnalités » (conversion de Features.tsx). */
@Component({
  selector: 'app-features',
  imports: [LucideDynamicIcon, RevealDirective],
  template: `
    <section id="features" class="py-32 bg-white dark:bg-black border-y border-black/5 dark:border-white/5 relative overflow-hidden">
      <div class="container mx-auto px-6 relative z-10">
        <div class="max-w-3xl mb-24">
          <h2 appReveal="left" class="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-6 block">
            {{ i18n.dict().Features.badge }}
          </h2>
          <h3 appReveal class="font-display text-4xl md:text-6xl font-black text-black dark:text-white mb-8 tracking-tight leading-[1.1]">
            {{ i18n.dict().Features.title }}
          </h3>
          <p appReveal class="text-xl md:text-2xl text-black/50 dark:text-white/50 font-light leading-relaxed">
            {{ i18n.dict().Features.description }}
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10">
          @for (feature of features(); track $index) {
            <div appReveal [revealDelay]="$index * 50" [revealDuration]="600"
                 class="group p-1 bg-gradient-to-b from-black/[0.03] to-transparent dark:from-white/[0.03] dark:to-transparent rounded-[32px] border border-black/5 dark:border-white/5 transition-colors hover:border-[#0070f3]/30 hover:-translate-y-2">
              <div class="bg-white dark:bg-[#050505] p-10 rounded-[28px] h-full flex flex-col items-start transition-colors group-hover:bg-[#f8faff] dark:group-hover:bg-[#000814]">
                <div class="w-14 h-14 rounded-2xl bg-black/5 dark:bg-white/5 text-black dark:text-white flex items-center justify-center mb-10 transition-all duration-300 group-hover:bg-[#0070f3] group-hover:text-white group-hover:shadow-[0_10px_20px_rgba(0,112,243,0.2)]">
                  <svg [lucideIcon]="feature.icon" [size]="28" [strokeWidth]="1.5" />
                </div>
                <h4 class="text-2xl font-bold text-black dark:text-white mb-5 tracking-tight">{{ feature.title }}</h4>
                <p class="text-black/50 dark:text-white/50 leading-relaxed font-light text-base lg:text-lg">{{ feature.description }}</p>
              </div>
            </div>
          }
        </div>
      </div>
    </section>
  `,
})
export class FeaturesComponent {
  protected readonly i18n = inject(I18nService);

  protected readonly features = computed(() => {
    const d = this.i18n.dict().Features;
    return [
      { title: d.f1_title, description: d.f1_desc, icon: ICONS.Users },
      { title: d.f2_title, description: d.f2_desc, icon: ICONS.Calendar },
      { title: d.f3_title, description: d.f3_desc, icon: ICONS.CreditCard },
      { title: d.f4_title, description: d.f4_desc, icon: ICONS.BookOpen },
      { title: d.f5_title, description: d.f5_desc, icon: ICONS.Bell },
      { title: d.f6_title, description: d.f6_desc, icon: ICONS.ChartLine },
    ];
  });
}
