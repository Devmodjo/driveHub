import { Component, inject } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { RevealDirective } from '../../../directives/reveal.directive';
import { ICONS } from '../../../shared/icons';
import { I18nService } from '../i18n/i18n.service';

/** Section « À propos » (conversion de About.tsx). */
@Component({
  selector: 'app-about',
  imports: [LucideAngularModule, RevealDirective],
  template: `
    <section id="about" class="py-32 bg-[#fafafa] dark:bg-[#080808] border-b border-black/5 dark:border-white/5 overflow-hidden">
      <div class="container mx-auto px-6">
        <div class="flex flex-col lg:flex-row items-center gap-16 lg:gap-24 relative">
          <div appReveal="left" class="lg:w-1/2">
            <h2 class="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-6 block">{{ i18n.dict().About.badge }}</h2>
            <h3 class="font-display text-4xl md:text-5xl lg:text-6xl font-black mb-10 text-black dark:text-white leading-[1.1] tracking-tight">{{ i18n.dict().About.title }}</h3>
            <div class="space-y-6">
              <p class="text-black/60 dark:text-white/60 text-lg md:text-xl leading-relaxed font-light">{{ i18n.dict().About.description1 }}</p>
              <p class="text-black/60 dark:text-white/60 text-lg md:text-xl leading-relaxed font-light">{{ i18n.dict().About.description2 }}</p>
            </div>
          </div>

          <div appReveal [revealDuration]="1000" class="lg:w-1/2 w-full relative">
            <div class="p-10 md:p-14 rounded-[40px] bg-black dark:bg-[#050505] border border-white/5 text-white shadow-2xl relative overflow-hidden group w-full lg:max-w-lg lg:ml-auto">
              <div class="absolute top-0 right-0 w-64 h-64 bg-[#0070f3]/25 blur-[80px] rounded-full group-hover:scale-125 transition-transform duration-700 pointer-events-none"></div>
              <div class="absolute -bottom-10 -left-10 w-48 h-48 bg-[#4096ff]/20 blur-[60px] rounded-full pointer-events-none"></div>

              <lucide-icon [img]="icons.Target" class="text-[#0070f3] mb-8 relative z-10" [size]="48" [strokeWidth]="1.5" />

              <h4 class="text-3xl lg:text-4xl font-black mb-6 relative z-10 tracking-tight leading-[1.1]">{{ i18n.dict().About.banner_title }}</h4>
              <p class="text-white/60 leading-relaxed font-light text-lg relative z-10 mb-8">{{ i18n.dict().About.banner_desc }}</p>

              <div class="pt-8 border-t border-white/10 relative z-10 flex items-center justify-between">
                <div class="flex -space-x-3">
                  <div class="w-10 h-10 rounded-full bg-white/10 border-2 border-black flex items-center justify-center text-[10px] font-bold">PRO</div>
                  <div class="w-10 h-10 rounded-full bg-[#0070f3]/20 border-2 border-black flex items-center justify-center text-[10px] font-bold text-[#0070f3]">B2B</div>
                </div>
                <div class="flex gap-1">
                  @for (i of stars; track i) {
                    <lucide-icon [img]="icons.CircleCheck" [size]="16" class="text-[#0070f3]" />
                  }
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  `,
})
export class AboutComponent {
  protected readonly icons = ICONS;
  protected readonly i18n = inject(I18nService);
  protected readonly stars = [0, 1, 2, 3, 4];
}
