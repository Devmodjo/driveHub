import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { RevealDirective } from '../../../../directives/reveal.directive';
import { PublicSchool } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { SessionService } from '../../../../services/session-service/session.service';
import { isMissingDocumentsError } from '../../../../shared/documents';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';

/**
 * Catalogue public « Trouver une auto-école » : liste des auto-écoles approuvées par la plateforme
 * (registre du schéma public). Un élève connecté peut demander à rejoindre une auto-école.
 * Le backend exige d'abord les justificatifs (élève : pièce d'identité ; moniteur : pièce d'identité
 * et CAPEC) : s'il en manque, le message d'erreur propose un lien vers la page où les ajouter.
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
          <input class="field-input pl-11" type="search" placeholder="Nom de l'auto-école ou ville"
                 [ngModel]="query()" (ngModelChange)="query.set($event)" />
        </div>

        @if (message()) { <div class="alert-success mb-8">{{ message() }}</div> }
        @if (error()) {
          <div class="alert-error mb-8" role="alert">
            {{ error() }}
            <!-- Justificatifs manquants : lien direct vers la page où on les ajoute -->
            @if (missingDocuments()) {
              <a routerLink="/dashboard/bienvenue" class="mt-2 flex items-center gap-1.5 font-bold underline">
                Ajouter mes justificatifs <svg [lucideIcon]="icons.ArrowRight" [size]="15" />
              </a>
            }
          </div>
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
              <article class="premium-card rounded-[28px] p-8 flex flex-col">
                <div class="w-12 h-12 rounded-2xl bg-[#0070f3]/10 text-[#0070f3] flex items-center justify-center mb-6">
                  <svg [lucideIcon]="icons.School" [size]="24" [strokeWidth]="1.5" />
                </div>
                <h3 class="text-xl font-bold text-black dark:text-white mb-3 tracking-tight">{{ school.name }}</h3>
                <ul class="space-y-2 text-sm text-black/60 dark:text-white/60 font-light mb-8 grow">
                  <li class="flex items-center gap-2"><svg [lucideIcon]="icons.MapPin" [size]="15" /> {{ school.address }}, {{ school.city }}</li>
                  @if (school.phoneNumber) {
                    <li class="flex items-center gap-2"><svg [lucideIcon]="icons.Phone" [size]="15" /> {{ school.phoneNumber }}</li>
                  }
                  @if (school.email) {
                    <li class="flex items-center gap-2"><svg [lucideIcon]="icons.Mail" [size]="15" /> {{ school.email }}</li>
                  }
                </ul>
                <button class="btn-primary w-full" [disabled]="sending() === school.id" (click)="join(school)">
                  {{ sending() === school.id ? 'Envoi...' : 'Demander à m\\'inscrire' }}
                </button>
              </article>
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
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  protected readonly schools = signal<PublicSchool[]>([]);
  protected readonly loading = signal(true);
  protected readonly query = signal('');
  protected readonly sending = signal<string | null>(null);
  protected readonly message = signal('');
  protected readonly error = signal('');
  /** Le backend a refusé la demande car un justificatif manque (pièce d'identité, CAPEC). */
  protected readonly missingDocuments = signal(false);

  protected readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    return q
      ? this.schools().filter((s) => `${s.name} ${s.city} ${s.address}`.toLowerCase().includes(q))
      : this.schools();
  });

  constructor() {
    this.api.publicSchools().subscribe({
      next: (schools) => { this.schools.set(schools ?? []); this.loading.set(false); },
      error: (err) => { this.error.set(errorMessage(err)); this.loading.set(false); },
    });
  }

  protected join(school: PublicSchool): void {
    if (!this.session.isLoggedIn()) {
      this.router.navigate(['/connexion'], { queryParams: { redirect: '/auto-ecoles' } });
      return;
    }
    const role = this.session.role();
    if (!role) return;
    this.sending.set(school.id);
    this.message.set('');
    this.error.set('');
    this.missingDocuments.set(false);
    this.api.requestJoin(school.id, role).subscribe({
      next: () => {
        this.sending.set(null);
        this.message.set(`Demande envoyée à ${school.name}. Suivez son statut depuis votre espace.`);
      },
      error: (err) => {
        this.sending.set(null);
        const message = errorMessage(err);
        this.error.set(message);
        this.missingDocuments.set(isMissingDocumentsError(err?.status ?? 0, message));
      },
    });
  }
}
