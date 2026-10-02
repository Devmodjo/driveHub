import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { LucideDynamicIcon, LucideIcon } from '@lucide/angular';
import { Reservation, SchoolSubscription, Student, UserDocument } from '../../../../interfaces/drivehub.models';
import { DocumentService } from '../../../../services/document-service/document.service';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { documentStatusLabel } from '../../../../shared/documents';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { formatAmount, formatDate, formatDateTime, label } from '../../shared/labels';
import { PageHeaderComponent } from '../../shared/page-header.component';
import { StatusBadgeComponent } from '../../shared/status-badge.component';

interface Kpi {
  label: string;
  value: string;
  icon: LucideIcon;
  link: string;
}

/**
 * Vue d'ensemble : indicateurs de l'auto-école (moniteur) ou dossier de formation (élève).
 * Pour le responsable, un bandeau discret rappelle la fin de la période d'essai (abonnement TRIAL).
 * Rien sur le paiement pour l'instant : la facturation n'est pas encore activée.
 */
@Component({
  selector: 'app-overview',
  imports: [RouterLink, LucideDynamicIcon, PageHeaderComponent, StatusBadgeComponent],
  template: `
    <app-page-header title="Vue d'ensemble" [subtitle]="session.isMonitor() ? 'Activité de votre auto-école' : 'Votre formation en un coup d\\'oeil'" />

    @if (error()) { <div class="alert-error mb-6">{{ error() }}</div> }

    @if (trial(); as t) {
      <div class="mb-6 flex items-start sm:items-center gap-3 rounded-2xl border border-[#0070f3]/20 bg-[#0070f3]/[0.06] dark:bg-[#0070f3]/10 px-4 py-3 text-sm text-black/80 dark:text-white/80" role="status">
        <svg [lucideIcon]="icons.Clock" [size]="18" class="shrink-0 text-[#0070f3] mt-0.5 sm:mt-0" />
        <p>
          <strong class="font-bold text-black dark:text-white">Période d'essai :</strong>
          {{ trialDaysText(t) }}@if (t.trialEndsAt) { (fin le {{ formatDate(t.trialEndsAt) }})}.
        </p>
      </div>
    }

    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-5 mb-10">
      @for (kpi of kpis(); track kpi.label) {
        <a [routerLink]="kpi.link" class="premium-card rounded-[24px] p-6 block">
          <div class="w-11 h-11 rounded-xl bg-[#0070f3]/10 text-[#0070f3] flex items-center justify-center mb-5">
            <svg [lucideIcon]="kpi.icon" [size]="20" />
          </div>
          <p class="text-3xl font-black tracking-tight">{{ kpi.value }}</p>
          <p class="text-sm text-black/50 dark:text-white/50 mt-1">{{ kpi.label }}</p>
        </a>
      }
    </div>

    @if (profile(); as p) {
      <div class="premium-card rounded-[28px] p-8 mb-10">
        <h3 class="text-lg font-bold mb-5">Mon dossier</h3>
        <dl class="grid grid-cols-1 md:grid-cols-3 gap-5 text-sm">
          <div><dt class="field-label">Nom</dt><dd>{{ p.firstname }} {{ p.lastname }}</dd></div>
          <div><dt class="field-label">Téléphone</dt><dd>{{ p.phoneNumber }}</dd></div>
          <div><dt class="field-label">Catégorie de permis</dt><dd>{{ p.licenseCategory ?? 'À définir avec votre auto-école' }}</dd></div>
          <div><dt class="field-label">Ville</dt><dd>{{ p.residenceCity }}</dd></div>
          <div><dt class="field-label">Pièce d'identité</dt><dd>{{ cniStatus() }}</dd></div>
        </dl>
      </div>
    }

    <div class="premium-card rounded-[28px] p-6 overflow-x-auto">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-bold">Prochains créneaux</h3>
        <a routerLink="/dashboard/reservations" class="text-sm text-[#0070f3] font-semibold">Tout voir</a>
      </div>
      @if (upcoming().length === 0) {
        <p class="text-sm text-black/50 dark:text-white/50">Aucun créneau à venir.</p>
      } @else {
        <table class="data-table">
          <thead><tr><th>Date</th><th>Type</th><th>Élève</th><th>Moniteur</th><th>Statut</th></tr></thead>
          <tbody>
            @for (r of upcoming(); track r.id) {
              <tr>
                <td>{{ formatDateTime(r.dateTime) }}</td><td>{{ label(r.types) }}</td>
                <td>{{ r.studentFirstname }} {{ r.studentLastname }}</td><td>{{ r.monitorFirstname }} {{ r.monitorLastname }}</td>
                <td><app-status-badge [value]="r.reservationStatus" /></td>
              </tr>
            }
          </tbody>
        </table>
      }
    </div>
  `,
})
export class OverviewComponent {
  protected readonly icons = ICONS;
  protected readonly session = inject(SessionService);
  private readonly api = inject(SchoolApiService);
  private readonly documents = inject(DocumentService);
  protected readonly label = label;
  protected readonly formatDate = formatDate;
  protected readonly formatDateTime = formatDateTime;

  protected readonly kpis = signal<Kpi[]>([]);
  protected readonly upcoming = signal<Reservation[]>([]);
  protected readonly profile = signal<Student | null>(null);
  protected readonly error = signal('');
  /** Abonnement en période d'essai (null sinon, ou si l'utilisateur n'est pas le responsable). */
  protected readonly trial = signal<SchoolSubscription | null>(null);
  /** Statut de la pièce d'identité de l'élève (« Vérifié », « En vérification »...). */
  protected readonly cniStatus = signal('-');

  constructor() {
    const onError = (err: unknown) => this.error.set(errorMessage(err));

    this.api.reservations(0, 100).subscribe({
      next: (page) => {
        const now = Date.now();
        this.upcoming.set(page.content
          .filter((r) => r.reservationStatus !== 'CANCELLED' && new Date(r.dateTime).getTime() >= now)
          .sort((a, b) => a.dateTime.localeCompare(b.dateTime))
          .slice(0, 5));
      },
      error: onError,
    });

    if (this.session.isMonitor()) {
      // Seul le responsable a accès à l'abonnement : pour un autre moniteur, l'erreur est ignorée
      this.api.mySubscription().subscribe({
        next: (sub) => this.trial.set(sub?.status === 'TRIAL' ? sub : null),
        error: () => this.trial.set(null),
      });
      forkJoin({
        students: this.api.students(0, 1),
        vehicles: this.api.vehicles(0, 1),
        joins: this.api.pendingJoinRequests(0, 1),
        summary: this.api.paymentSummary(),
      }).subscribe({
        next: ({ students, vehicles, joins, summary }) => this.kpis.set([
          { label: 'Élèves inscrits', value: String(students.totalElements), icon: ICONS.Users, link: '/dashboard/eleves' },
          { label: 'Demandes en attente', value: String(joins.totalElements), icon: ICONS.UserPlus, link: '/dashboard/demandes' },
          { label: 'Véhicules', value: String(vehicles.totalElements), icon: ICONS.Car, link: '/dashboard/vehicules' },
          { label: 'Encaissé, ' + formatAmount(summary.totalPending) + ' en attente', value: formatAmount(summary.totalValidated), icon: ICONS.Wallet, link: '/dashboard/paiements' },
        ]),
        error: onError,
      });
    } else {
      this.api.myStudentProfile().subscribe({ next: (p) => this.profile.set(p), error: onError });
      this.documents.myDocuments().subscribe({
        next: (docs) => this.cniStatus.set(documentStatusLabel(docs?.find((d: UserDocument) => d.type === 'CNI')?.status)),
        error: () => this.cniStatus.set('-'),
      });
      forkJoin({ payments: this.api.payments(0, 100), exams: this.api.myExamInscriptions() }).subscribe({
        next: ({ payments, exams }) => {
          const paid = payments.content.filter((p) => p.paymentStatus === 'VALIDATE').reduce((sum, p) => sum + Number(p.amount), 0);
          this.kpis.set([
            { label: 'Total payé', value: formatAmount(paid), icon: ICONS.Wallet, link: '/dashboard/paiements' },
            { label: 'Paiements en attente', value: String(payments.content.filter((p) => p.paymentStatus === 'PENDING').length), icon: ICONS.CreditCard, link: '/dashboard/paiements' },
            { label: 'Inscriptions aux examens', value: String(exams.length), icon: ICONS.GraduationCap, link: '/dashboard/examens' },
          ]);
        },
        error: onError,
      });
    }
  }

  /** « 15 jours restants », « 1 jour restant » ou « dernier jour ». */
  protected trialDaysText(sub: SchoolSubscription): string {
    const days = Math.max(0, sub.trialDaysLeft ?? 0);
    if (days === 0) return 'dernier jour';
    return days === 1 ? '1 jour restant' : `${days} jours restants`;
  }
}
