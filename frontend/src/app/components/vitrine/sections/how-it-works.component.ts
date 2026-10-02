import { Component, computed, inject } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { RevealDirective } from '../../../directives/reveal.directive';
import { ICONS } from '../../../shared/icons';
import { I18nService } from '../i18n/i18n.service';

/** Section « Comment ça marche » (conversion de HowItWorks.tsx). */
@Component({
  selector: 'app-how-it-works',
  imports: [LucideAngularModule, RevealDirective],
  template: `
    <section id="how-it-works" class="py-24 bg-white dark:bg-black border-y border-black/5 dark:border-white/5">
      <div class="container mx-auto px-6">
        <div appReveal class="text-center max-w-3xl mx-auto mb-16">
          <h2 class="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-4">{{ i18n.dict().HowItWorks.badge }}</h2>
          <h3 class="font-display text-4xl md:text-5xl lg:text-6xl font-black text-black dark:text-white mb-6 tracking-tight leading-[1.1]">{{ i18n.dict().HowItWorks.title }}</h3>
          <p class="text-xl md:text-2xl text-black/60 dark:text-white/60 font-light leading-relaxed">
            {{ i18n.dict().HowItWorks.description }}
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-12 max-w-5xl mx-auto">
          @for (step of steps(); track $index; let last = $last) {
            <div appReveal [revealDelay]="$index * 100" [revealDuration]="500" class="relative text-center group">
              <div class="w-20 h-20 rounded-3xl bg-black/5 dark:bg-white/5 border border-black/5 dark:border-white/5 text-[#0070f3] flex items-center justify-center mx-auto mb-8 shadow-sm group-hover:bg-[#0070f3] group-hover:text-white group-hover:border-[#0070f3]/50 transition-all duration-300">
                <lucide-icon [img]="step.icon" [size]="32" [strokeWidth]="1.5" />
              </div>
              <h4 class="text-2xl font-bold text-black dark:text-white mb-4">{{ step.title }}</h4>
              <p class="text-black/60 dark:text-white/60 leading-relaxed font-light">{{ step.description }}</p>
              @if (!last) {
                <div class="hidden lg:block absolute top-10 left-[70%] w-[60%] h-[1px] bg-black/10 dark:bg-white/10 border-0"></div>
              }
            </div>
          }
        </div>
      </div>
    </section>
  `,
})
export class HowItWorksComponent {
  protected readonly i18n = inject(I18nService);

  protected readonly steps = computed(() => {
    const d = this.i18n.dict().HowItWorks;
    return [
      { title: d.step1_title, description: d.step1_desc, icon: ICONS.UserPlus },
      { title: d.step2_title, description: d.step2_desc, icon: ICONS.Settings },
      { title: d.step3_title, description: d.step3_desc, icon: ICONS.CircleCheck },
    ];
  });
}
