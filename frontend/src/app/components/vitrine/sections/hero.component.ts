import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { RevealDirective } from '../../../directives/reveal.directive';
import { ICONS } from '../../../shared/icons';
import { I18nService } from '../i18n/i18n.service';

/** Section d'accueil (conversion de Hero.tsx). */
@Component({
  selector: 'app-hero',
  imports: [RouterLink, LucideDynamicIcon, RevealDirective],
  template: `
    <section class="relative min-h-[90vh] flex items-center pt-32 pb-20 overflow-hidden">
      <img src="/images/hero-bg-2.jpg" alt="Arrière-plan conducteur" class="absolute inset-0 w-full h-full object-cover" />
      <div class="absolute inset-0 z-0 bg-gradient-to-t from-black via-black/60 to-black/10"></div>

      <div class="container mx-auto px-6 relative z-10">
        <div class="max-w-5xl mx-auto text-center flex flex-col items-center">
          <div appReveal="down"
               class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white/10 border border-white/20 text-[10px] font-black uppercase tracking-[0.2em] text-white/90 mb-12 backdrop-blur-md shadow-2xl">
            Digitalisation Auto-École
          </div>

          <h1 appReveal="scale" [revealDelay]="100"
              class="font-display text-5xl md:text-7xl lg:text-[96px] font-black tracking-tighter text-white mb-8 leading-[0.95] drop-shadow-2xl selection:bg-white selection:text-[#0070f3]">
            {{ i18n.dict().Hero.title1 }} <br />
            <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-200 via-white to-gray-300 drop-shadow-lg">
              {{ i18n.dict().Hero.title2 }}
            </span>
          </h1>

          <p appReveal [revealDelay]="200"
             class="text-lg md:text-2xl text-white/80 mb-12 leading-relaxed max-w-3xl mx-auto font-light">
            {{ i18n.dict().Hero.description }}
          </p>

          <div appReveal [revealDelay]="300" class="flex flex-col sm:flex-row gap-4 justify-center items-center w-full sm:w-auto">
            <a routerLink="/inscription"
               class="w-full sm:w-auto bg-[#0070f3] text-white px-10 py-5 rounded-full font-black text-lg transition-all duration-200 shadow-[0_0_40px_rgba(0,112,243,0.5)] hover:shadow-[0_0_60px_rgba(0,112,243,0.7)] hover:bg-[#0051af] hover:scale-105 hover:-translate-y-0.5 active:scale-95 flex items-center justify-center gap-3 backdrop-blur-md">
              {{ i18n.dict().Hero.btn_manage }} <svg [lucideIcon]="icons.ArrowRight" [size]="22" [strokeWidth]="3" />
            </a>
            <a routerLink="/auto-ecoles"
               class="w-full sm:w-auto px-10 py-5 border-2 border-white/40 hover:border-white/60 hover:bg-white/15 hover:scale-[1.02] hover:-translate-y-0.5 active:scale-[0.98] text-white rounded-full font-bold text-lg transition-all duration-200 flex items-center justify-center gap-3 backdrop-blur-md">
              {{ i18n.dict().Hero.btn_find }} <svg [lucideIcon]="icons.Search" [size]="22" [strokeWidth]="2.5" />
            </a>
          </div>
        </div>
      </div>
    </section>
  `,
})
export class HeroComponent {
  protected readonly icons = ICONS;
  protected readonly i18n = inject(I18nService);
}
