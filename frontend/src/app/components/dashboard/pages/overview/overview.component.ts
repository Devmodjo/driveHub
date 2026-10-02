import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { LucideAngularModule, LucideIconData } from 'lucide-angular';
import { Reservation, Student } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { formatAmount, formatDateTime, label } from '../../shared/labels';
import { PageHeaderComponent } from '../../shared/page-header.component';
import { StatusBadgeComponent } from '../../shared/status-badge.component';

interface Kpi {
  label: string;
  value: string;
  icon: LucideIconData;
  link: string;
}

/** Vue d'ensemble : indicateurs de l'auto-école (moniteur) ou dossier de formation (élève). */
@Component({
  selector: 'app-overview',
  imports: [RouterLink, LucideAngularModule, PageHeaderComponent, StatusBadgeComponent],
  template: `
    <app-page-header title="Vue d'ensemble" [subtitle]="session.isMonitor() ? 'Activité de votre auto-école' : 'Votre formation en un coup d\\'oeil'" />

    @if (error()) { <div class="alert-error mb-6">{{ error() }}</div> }

    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-5 mb-10">
      @for (kpi of kpis(); track kpi.label) {
        <a [routerLink]="kpi.link" class="premium-card rounded-[24px] p-6 block">
          <div class="w-11 h-11 rounded-xl bg-[#0070f3]/10 text-[#0070f3] flex items-center justify-center mb-5">
            <lucide-icon [img]="kpi.icon" [size]="20" />
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
          <div><dt class="field-label">CNI</dt><dd>{{ p.cniRectoUrl ? 'Fournie' : 'À fournir à votre auto-école' }}</dd></div>
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
  protected readonly session = inject(SessionService);
  private readonly api = inject(SchoolApiService);
  protected readonly label = label;
  protected readonly formatDateTime = formatDateTime;

  protected readonly kpis = signal<Kpi[]>([]);
  protected readonly upcoming = signal<Reservation[]>([]);
  protected readonly profile = signal<Student | null>(null);
  protected readonly error = signal('');

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
}
