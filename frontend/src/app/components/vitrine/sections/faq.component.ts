import { Component, computed, inject, signal } from '@angular/core';
import { LucideDynamicIcon } from '@lucide/angular';
import { RevealDirective } from '../../../directives/reveal.directive';
import { ICONS } from '../../../shared/icons';
import { I18nService } from '../i18n/i18n.service';

/** Section « Questions fréquentes » (conversion de FAQ.tsx). */
@Component({
  selector: 'app-faq',
  imports: [LucideDynamicIcon, RevealDirective],
  template: `
    <section id="faq" class="py-24 bg-white dark:bg-black">
      <div class="container mx-auto px-6">
        <div appReveal class="text-center max-w-3xl mx-auto mb-16">
          <h2 class="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-4">{{ i18n.dict().FAQ.badge }}</h2>
          <h3 class="font-display text-4xl md:text-5xl lg:text-6xl font-black text-black dark:text-white mb-6 tracking-tight leading-[1.1]">{{ i18n.dict().FAQ.title }}</h3>
          <p class="text-xl md:text-2xl text-black/60 dark:text-white/60 font-light leading-relaxed">{{ i18n.dict().FAQ.description }}</p>
        </div>

        <div appReveal [revealDelay]="200" [revealDuration]="600" class="max-w-3xl mx-auto space-y-4">
          @for (faq of faqs(); track $index) {
            <div class="bg-black/5 dark:bg-white/5 border border-black/5 dark:border-white/5 rounded-2xl overflow-hidden transition-all duration-300 hover:border-[#0070f3]/30">
              <button class="w-full px-6 py-6 flex items-center justify-between text-left group" (click)="toggle($index)">
                <span class="font-display font-black text-lg text-black dark:text-white group-hover:text-[#0070f3] transition-colors">{{ faq.question }}</span>
                <div class="w-8 h-8 rounded-full flex items-center justify-center bg-black/5 dark:bg-white/5 shrink-0 transition-transform duration-300"
                     [class]="activeIndex() === $index ? 'rotate-180 bg-[#0070f3] text-white' : 'text-black/50 dark:text-white/50'">
                  <svg [lucideIcon]="icons.ChevronDown" [size]="18" />
                </div>
              </button>
              @if (activeIndex() === $index) {
                <div class="animate-expand">
                  <div class="px-6 pb-6 pt-2 text-black/60 dark:text-white/60 text-base leading-relaxed font-light">{{ faq.answer }}</div>
                </div>
              }
            </div>
          }
        </div>
      </div>
    </section>
  `,
})
export class FaqComponent {
  protected readonly icons = ICONS;
  protected readonly i18n = inject(I18nService);
  protected readonly activeIndex = signal<number | null>(null);

  protected readonly faqs = computed(() => {
    const d = this.i18n.dict().FAQ;
    return [
      { question: d.q1, answer: d.a1 },
      { question: d.q2, answer: d.a2 },
      { question: d.q3, answer: d.a3 },
      { question: d.q4, answer: d.a4 },
    ];
  });

  protected toggle(index: number): void {
    this.activeIndex.set(this.activeIndex() === index ? null : index);
  }
}
