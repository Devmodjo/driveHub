import { DatePipe } from '@angular/common';
import { Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { PublicSchool } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { SeoService } from '../../../../services/seo-service/seo.service';
import { SessionService } from '../../../../services/session-service/session.service';
import { isMissingDocumentsError } from '../../../../shared/documents';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { SpamHintComponent } from '../../../../shared/spam-hint.component';

/**
 * Page publique d'une auto-école : /auto-ecoles/{slug} (ex : /auto-ecoles/auto-ecole-le-volant-douala).
 *
 * - Accessible sans compte ; seules les auto-écoles validées par DriveHub sont visibles.
 * - Présentation, coordonnées (téléphone, WhatsApp, email, site, plan) et bouton « Demander à m'inscrire ».
 * - Référencement : titre, description et données structurées « DrivingSchool » (schema.org) pour Google.
 */
@Component({
  selector: 'app-school-detail',
  imports: [RouterLink, LucideDynamicIcon, DatePipe, SpamHintComponent],
  template: `
    <section class="pt-32 pb-24 bg-white dark:bg-black min-h-screen">
      <div class="container mx-auto px-6 max-w-5xl">
        <a routerLink="/auto-ecoles" class="inline-flex items-center gap-2 text-sm font-semibold text-black/60 dark:text-white/60 hover:text-[#0070f3] mb-8">
          <svg [lucideIcon]="icons.ArrowLeft" [size]="16" /> Toutes les auto-écoles
        </a>

        @if (loading()) {
          <p class="text-black/50 dark:text-white/50">Chargement...</p>
        } @else if (!school()) {
          <div class="premium-card rounded-[28px] p-10 text-center">
            <h1 class="font-display text-2xl font-black text-black dark:text-white mb-3">Auto-école introuvable</h1>
            <p class="text-black/50 dark:text-white/50 mb-6">Elle n'existe pas ou n'est plus présente sur DriveHub.</p>
            <a routerLink="/auto-ecoles" class="btn-primary">Voir les auto-écoles</a>
          </div>
        } @else {
          @let s = school()!;
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <!-- Présentation -->
            <article class="lg:col-span-2">
              <div class="flex items-start gap-5 mb-8">
                <div class="w-16 h-16 shrink-0 rounded-2xl bg-[#0070f3]/10 text-[#0070f3] flex items-center justify-center">
                  <svg [lucideIcon]="icons.School" [size]="30" [strokeWidth]="1.5" />
                </div>
                <div class="min-w-0">
                  <h1 class="font-display text-3xl md:text-5xl font-black text-black dark:text-white tracking-tight leading-[1.1] break-words">{{ s.name }}</h1>
                  <p class="mt-3 flex flex-wrap items-center gap-x-4 gap-y-2 text-black/60 dark:text-white/60">
                    <span class="inline-flex items-center gap-1.5"><svg [lucideIcon]="icons.MapPin" [size]="16" /> {{ s.city }}{{ s.country ? ', ' + s.country : '' }}</span>
                    <span class="inline-flex items-center gap-1.5 text-[#0070f3] font-semibold"><svg [lucideIcon]="icons.ShieldCheck" [size]="16" /> Vérifiée par DriveHub</span>
                  </p>
                </div>
              </div>

              <div class="premium-card rounded-[28px] p-6 md:p-8">
                <h2 class="text-lg font-bold text-black dark:text-white mb-4">Présentation</h2>
                @if (s.description) {
                  <p class="text-black/70 dark:text-white/70 leading-relaxed whitespace-pre-line break-words">{{ s.description }}</p>
                } @else {
                  <p class="text-black/50 dark:text-white/50">
                    {{ s.name }} est une auto-école de {{ s.city }}, vérifiée par l'équipe DriveHub.
                    Inscrivez-vous en ligne et suivez votre formation depuis votre téléphone.
                  </p>
                }
                <p class="mt-6 text-sm text-black/40 dark:text-white/40">Sur DriveHub depuis le {{ s.createdAt | date: 'dd/MM/yyyy' }}</p>
              </div>
            </article>

            <!-- Inscription et coordonnées -->
            <aside class="space-y-6">
              <div class="premium-card rounded-[28px] p-6">
                <h2 class="text-lg font-bold text-black dark:text-white mb-2">S'inscrire</h2>
                <p class="text-sm text-black/60 dark:text-white/60 mb-5">
                  Envoyez votre demande : l'auto-école la valide, puis vous réservez vos leçons et suivez vos cours en ligne.
                </p>
                @if (message()) {
                  <div class="alert-success mb-3">{{ message() }}</div>
                  <app-spam-hint class="mb-4" />
                }
                @if (error()) {
                  <div class="alert-error mb-4" role="alert">
                    {{ error() }}
                    @if (missingDocuments()) {
                      <a routerLink="/dashboard/bienvenue" class="mt-2 flex items-center gap-1.5 font-bold underline">
                        Ajouter mes justificatifs <svg [lucideIcon]="icons.ArrowRight" [size]="15" />
                      </a>
                    }
                  </div>
                }
                <button class="btn-primary w-full" [disabled]="sending() || !!message()" (click)="join(s)">
                  {{ sending() ? 'Envoi...' : 'Demander à m\\'inscrire' }}
                </button>
              </div>

              <div class="premium-card rounded-[28px] p-6">
                <h2 class="text-lg font-bold text-black dark:text-white mb-4">Coordonnées</h2>
                <ul class="space-y-4 text-sm">
                  <li>
                    <a [href]="mapUrl()" target="_blank" rel="noopener" class="flex items-start gap-3 text-black/70 dark:text-white/70 hover:text-[#0070f3]">
                      <svg [lucideIcon]="icons.MapPin" [size]="18" class="shrink-0 mt-0.5" />
                      <span>{{ s.address }}, {{ s.city }}<span class="block text-[#0070f3] font-semibold mt-0.5">Voir sur la carte</span></span>
                    </a>
                  </li>
                  @if (s.phoneNumber) {
                    <li><a [href]="'tel:' + phoneDigits(s.phoneNumber)" class="flex items-center gap-3 text-black/70 dark:text-white/70 hover:text-[#0070f3]">
                      <svg [lucideIcon]="icons.Phone" [size]="18" class="shrink-0" /> {{ s.phoneNumber }}</a></li>
                  }
                  @if (s.whatsappNumber) {
                    <li><a [href]="whatsappUrl()" target="_blank" rel="noopener" class="flex items-center gap-3 text-black/70 dark:text-white/70 hover:text-[#0070f3]">
                      <svg [lucideIcon]="icons.Send" [size]="18" class="shrink-0" /> WhatsApp : {{ s.whatsappNumber }}</a></li>
                  }
                  @if (s.email) {
                    <li><a [href]="'mailto:' + s.email" class="flex items-center gap-3 text-black/70 dark:text-white/70 hover:text-[#0070f3] break-all">
                      <svg [lucideIcon]="icons.Mail" [size]="18" class="shrink-0" /> {{ s.email }}</a></li>
                  }
                  @if (websiteUrl()) {
                    <li><a [href]="websiteUrl()" target="_blank" rel="noopener nofollow" class="flex items-center gap-3 text-black/70 dark:text-white/70 hover:text-[#0070f3] break-all">
                      <svg [lucideIcon]="icons.Globe" [size]="18" class="shrink-0" /> Site web</a></li>
                  }
                </ul>
              </div>
            </aside>
          </div>
        }
      </div>
    </section>
  `,
})
export class SchoolDetailComponent implements OnDestroy {
  protected readonly icons = ICONS;
  private readonly api = inject(SchoolApiService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly seo = inject(SeoService);
  private readonly slug = inject(ActivatedRoute).snapshot.paramMap.get('slug') ?? '';

  protected readonly school = signal<PublicSchool | null>(null);
  protected readonly loading = signal(true);
  protected readonly sending = signal(false);
  protected readonly message = signal('');
  protected readonly error = signal('');
  protected readonly missingDocuments = signal(false);

  /** Recherche Google Maps sur l'adresse (aucune clé d'API nécessaire). */
  protected readonly mapUrl = computed(() => {
    const s = this.school();
    return s ? 'https://www.google.com/maps/search/?api=1&query=' + encodeURIComponent(`${s.name}, ${s.address}, ${s.city}`) : '';
  });
  protected readonly whatsappUrl = computed(() => 'https://wa.me/' + this.phoneDigits(this.school()?.whatsappNumber ?? '').replace('+', ''));
  /** Lien du site web, complété par https:// si l'auto-école l'a saisi sans. */
  protected readonly websiteUrl = computed(() => {
    const url = this.school()?.websiteUrl?.trim();
    if (!url) return '';
    return /^https?:\/\//i.test(url) ? url : 'https://' + url;
  });

  constructor() {
    this.api.publicSchool(this.slug).subscribe({
      next: (school) => {
        this.school.set(school);
        this.loading.set(false);
        this.applySeo(school);
      },
      error: () => {
        this.loading.set(false);
        this.seo.setPage({ title: 'Auto-école introuvable - DriveHub', description: 'Cette auto-école n\'est pas présente sur DriveHub.', path: '/auto-ecoles' });
      },
    });
  }

  ngOnDestroy(): void {
    this.seo.setStructuredData(null);
  }

  protected phoneDigits(phone: string): string {
    return phone.replace(/[^\d+]/g, '');
  }

  protected join(school: PublicSchool): void {
    if (!this.session.isLoggedIn()) {
      // Après la connexion, retour sur cette page pour envoyer la demande
      this.router.navigate(['/connexion'], { queryParams: { redirect: `/auto-ecoles/${school.slug}` } });
      return;
    }
    const role = this.session.role();
    if (!role) return;
    this.sending.set(true);
    this.message.set('');
    this.error.set('');
    this.missingDocuments.set(false);
    this.api.requestJoin(school.id, role).subscribe({
      next: () => {
        this.sending.set(false);
        this.message.set(`Demande envoyée à ${school.name}. Vous serez prévenu par email de sa réponse ; suivez aussi son statut depuis votre espace.`);
      },
      error: (err) => {
        this.sending.set(false);
        const message = errorMessage(err);
        this.error.set(message);
        // Seuls les moniteurs fournissent des justificatifs : le lien ne concerne qu'eux
        this.missingDocuments.set(role === 'MONITOR' && isMissingDocumentsError(err?.status ?? 0, message));
      },
    });
  }

  /** Titre, description et fiche schema.org « DrivingSchool » lus par Google. */
  private applySeo(s: PublicSchool): void {
    const path = `/auto-ecoles/${s.slug}`;
    const description = s.description?.trim()
      || `${s.name}, auto-école à ${s.city} vérifiée par DriveHub. Inscription en ligne, réservation des leçons et paiement Mobile Money.`;
    this.seo.setPage({ title: `${s.name} - Auto-école à ${s.city} | DriveHub`, description, path });
    this.seo.setStructuredData({
      '@context': 'https://schema.org',
      '@type': 'DrivingSchool',
      name: s.name,
      description,
      url: location.origin + path,
      telephone: s.phoneNumber || undefined,
      email: s.email || undefined,
      sameAs: this.websiteUrl() ? [this.websiteUrl()] : undefined,
      address: {
        '@type': 'PostalAddress',
        streetAddress: s.address,
        addressLocality: s.city,
        addressCountry: s.country === 'Cameroun' ? 'CM' : s.country,
      },
    });
  }
}
