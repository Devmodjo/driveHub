import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { RevealDirective } from '../../../../directives/reveal.directive';
import { PublicSchool } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { SeoService } from '../../../../services/seo-service/seo.service';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';

/**
 * Catalogue public « Trouver une auto-école » : liste des auto-écoles validées par la plateforme.
 * Chaque fiche ouvre la page publique de l'auto-école (/auto-ecoles/{slug}), où se trouvent sa
 * présentation, ses coordonnées et le bouton « Demander à m'inscrire » (SchoolDetailComponent).
 */
@Component({
  selector: 'app-schools',
  imports: [FormsModule, RouterLink, LucideDynamicIcon, RevealDirective],
  template: `
    <section class="pt-36 pb-24 bg-white dark:bg-black min-h-screen">
      <div class="container mx-auto px-6">
        <div appReveal class="max-w-3xl mb-14">
          <h2 class="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-6 block">Catalogue</h2>
          <h1 class="font-display text-4xl md:text-6xl font-black text-black dark:text-white mb-6 tracking-tight leading-[1.1]">Trouver une auto-école</h1>
          <p class="text-xl text-black/50 dark:text-white/50 font-light leading-relaxed">
            Toutes les auto-écoles présentes sur DriveHub ont été vérifiées par notre équipe.
            Créez votre compte élève, puis envoyez votre demande d'inscription en un clic.
          </p>
        </div>

        <div class="relative max-w-xl mb-12">
          <svg [lucideIcon]="icons.Search" [size]="18" class="absolute left-4 top-1/2 -translate-y-1/2 text-black/40 dark:text-white/40" />
          <input class="field-input" style="padding-left: 2.75rem" type="search" placeholder="Nom de l'auto-école ou ville"
                 [ngModel]="query()" (ngModelChange)="query.set($event)" />
        </div>

        @if (error()) {
          <div class="alert-error mb-8" role="alert">{{ error() }}</div>
        }
        @if (loading()) {
          <p class="text-black/50 dark:text-white/50">Chargement des auto-écoles...</p>
        } @else if (filtered().length === 0) {
          <div class="premium-card rounded-[28px] p-10 text-center text-black/50 dark:text-white/50">
            Aucune auto-école ne correspond à votre recherche.
          </div>
        } @else {
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            @for (school of filtered(); track school.id) {
              <a [routerLink]="['/auto-ecoles', school.slug]"
                 class="premium-card rounded-[28px] p-8 flex flex-col group transition-transform hover:-translate-y-1">
                <div class="w-12 h-12 rounded-2xl bg-[#0070f3]/10 text-[#0070f3] flex items-center justify-center mb-6">
                  <svg [lucideIcon]="icons.School" [size]="24" [strokeWidth]="1.5" />
                </div>
                <h2 class="text-xl font-bold text-black dark:text-white mb-2 tracking-tight group-hover:text-[#0070f3] transition-colors">{{ school.name }}</h2>
                <p class="flex items-center gap-2 text-sm text-black/60 dark:text-white/60 mb-4">
                  <svg [lucideIcon]="icons.MapPin" [size]="15" /> {{ school.city }}
                </p>
                <p class="text-sm text-black/60 dark:text-white/60 font-light leading-relaxed line-clamp-3 mb-6 grow">
                  {{ school.description || 'Auto-école vérifiée par DriveHub. Inscription en ligne et suivi de formation sur mobile.' }}
                </p>
                <span class="inline-flex items-center gap-2 font-bold text-[#0070f3]">
                  Voir l'auto-école <svg [lucideIcon]="icons.ArrowRight" [size]="16" class="transition-transform group-hover:translate-x-1" />
                </span>
              </a>
            }
          </div>
        }

        <p class="mt-16 text-sm text-black/50 dark:text-white/50">
          Vous dirigez une auto-école ?
          <a routerLink="/inscription" [queryParams]="{ role: 'MONITOR' }" class="text-[#0070f3] font-semibold">Inscrivez votre établissement</a>.
        </p>
      </div>
    </section>
  `,
})
export class SchoolsComponent {
  protected readonly icons = ICONS;
  private readonly api = inject(SchoolApiService);

  protected readonly schools = signal<PublicSchool[]>([]);
  protected readonly loading = signal(true);
  protected readonly query = signal('');
  protected readonly error = signal('');

  protected readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    return q
      ? this.schools().filter((s) => `${s.name} ${s.city} ${s.address}`.toLowerCase().includes(q))
      : this.schools();
  });

  constructor() {
    inject(SeoService).setPage({
      title: 'Trouver une auto-école au Cameroun - DriveHub',
      description: 'Auto-écoles vérifiées à Douala, Yaoundé et partout au Cameroun : présentation, coordonnées et inscription en ligne.',
      path: '/auto-ecoles',
    });
    this.api.publicSchools().subscribe({
      next: (schools) => { this.schools.set(schools ?? []); this.loading.set(false); },
      error: (err) => { this.error.set(errorMessage(err)); this.loading.set(false); },
    });
  }
}
