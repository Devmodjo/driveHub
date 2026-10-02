import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Monitor, Reservation, ReservationType, Student, Vehicle } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { formatDateTime, label, toLocalDateTime } from '../../shared/labels';
import { PageHeaderComponent } from '../../shared/page-header.component';
import { StatusBadgeComponent } from '../../shared/status-badge.component';

/**
 * Réservations de créneaux (leçons de conduite, rendez-vous).
 * Le backend refuse un créneau qui chevauche (60 min) celui du même moniteur, élève ou véhicule,
 * et exige un véhicule disponible pour une leçon de conduite.
 */
@Component({
  selector: 'app-reservations',
  imports: [FormsModule, LucideAngularModule, PageHeaderComponent, StatusBadgeComponent],
  template: `
    <app-page-header title="Réservations" subtitle="Leçons de conduite et rendez-vous (créneaux de 60 minutes).">
      <button class="btn-primary" (click)="openForm()"><lucide-icon [img]="icons.Plus" [size]="16" /> Réserver un créneau</button>
    </app-page-header>
    @if (error()) { <div class="alert-error mb-6">{{ error() }}</div> }

    @if (formOpen()) {
      <form class="premium-card rounded-[24px] p-6 mb-8 grid grid-cols-1 md:grid-cols-3 gap-4 items-end" (ngSubmit)="create()">
        @if (isMonitor) {
          <div><label class="field-label" for="student">Élève</label>
            <select id="student" class="field-input" name="student" required [(ngModel)]="form.studentId">
              <option value="">Choisir...</option>
              @for (s of students(); track s.id) { <option [value]="s.id">{{ s.firstname }} {{ s.lastname }}</option> }
            </select></div>
        }
        <div><label class="field-label" for="type">Type</label>
          <select id="type" class="field-input" name="type" [(ngModel)]="form.types">
            <option value="CONDUITE">Leçon de conduite</option><option value="RENDEZVOUS">Rendez-vous</option><option value="ADMIN">Démarche administrative</option>
          </select></div>
        <div><label class="field-label" for="monitor">Moniteur</label>
          <select id="monitor" class="field-input" name="monitor" required [(ngModel)]="form.monitorId">
            <option value="">Choisir...</option>
            @for (m of monitors(); track m.id) { <option [value]="m.id">{{ m.firstname }} {{ m.lastname }}</option> }
          </select></div>
        @if (form.types === 'CONDUITE') {
          <div><label class="field-label" for="vehicle">Véhicule</label>
            <select id="vehicle" class="field-input" name="vehicle" required [(ngModel)]="form.vehicleId">
              <option value="">Choisir...</option>
              @for (v of vehicles(); track v.id) { <option [value]="v.id">{{ v.matriculation }} - {{ v.model }}</option> }
            </select></div>
        }
        <div><label class="field-label" for="date">Date et heure</label>
          <input id="date" class="field-input" type="datetime-local" name="date" required [(ngModel)]="form.dateTime" /></div>
        <div class="flex gap-2">
          <button type="submit" class="btn-primary">Réserver</button>
          <button type="button" class="btn-ghost" (click)="formOpen.set(false)">Annuler</button>
        </div>
      </form>
    }

    <div class="premium-card rounded-[24px] p-4 overflow-x-auto">
      <table class="data-table">
        <thead><tr><th>Date</th><th>Type</th><th>Élève</th><th>Moniteur</th><th>Véhicule</th><th>Statut</th><th></th></tr></thead>
        <tbody>
          @for (r of reservations(); track r.id) {
            <tr>
              <td class="whitespace-nowrap">{{ formatDateTime(r.dateTime) }}</td><td>{{ label(r.types) }}</td>
              <td>{{ r.studentFirstname }} {{ r.studentLastname }}</td><td>{{ r.monitorFirstname }} {{ r.monitorLastname }}</td>
              <td>{{ r.vehicleMatriculation ?? '-' }}</td><td><app-status-badge [value]="r.reservationStatus" /></td>
              <td class="text-right whitespace-nowrap space-x-2">
                @if (isMonitor && r.reservationStatus === 'PENDING') {
                  <button class="btn-small bg-[#0070f3] text-white" (click)="confirm(r)">Confirmer</button>
                }
                @if (r.reservationStatus !== 'CANCELLED') {
                  <button class="btn-small text-red-600 hover:bg-red-500/10" (click)="cancel(r)">Annuler</button>
                }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="7" class="text-center text-black/50 dark:text-white/50">Aucune réservation.</td></tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class ReservationsComponent {
  protected readonly icons = ICONS;
  protected readonly label = label;
  protected readonly formatDateTime = formatDateTime;
  private readonly api = inject(SchoolApiService);
  protected readonly isMonitor = inject(SessionService).isMonitor();

  protected readonly reservations = signal<Reservation[]>([]);
  protected readonly students = signal<Student[]>([]);
  protected readonly monitors = signal<Monitor[]>([]);
  protected readonly vehicles = signal<Vehicle[]>([]);
  protected readonly formOpen = signal(false);
  protected readonly error = signal('');
  protected form = { studentId: '', monitorId: '', vehicleId: '', dateTime: '', types: 'CONDUITE' as ReservationType };

  constructor() {
    this.load();
  }

  private load(): void {
    this.api.reservations(0, 200).subscribe({
      next: (p) => this.reservations.set([...p.content].sort((a, b) => b.dateTime.localeCompare(a.dateTime))),
      error: (err) => this.fail(err),
    });
  }

  protected openForm(): void {
    this.formOpen.set(true);
    this.api.monitors().subscribe({ next: (list) => this.monitors.set(list) });
    this.api.vehicles(0, 200).subscribe({ next: (p) => this.vehicles.set(p.content.filter((v) => v.state === 'DISPOSABLE')) });
    if (this.isMonitor) {
      this.api.students(0, 500).subscribe({ next: (p) => this.students.set(p.content) });
    }
  }

  protected create(): void {
    this.api.createReservation({
      studentId: this.isMonitor ? this.form.studentId : null,
      monitorId: this.form.monitorId,
      vehicleId: this.form.types === 'CONDUITE' ? this.form.vehicleId : null,
      dateTime: toLocalDateTime(this.form.dateTime),
      types: this.form.types,
    }).subscribe({
      next: () => { this.formOpen.set(false); this.error.set(''); this.load(); },
      error: (err) => this.fail(err),
    });
  }

  protected confirm(r: Reservation): void {
    this.api.confirmReservation(r.id).subscribe({ next: () => this.load(), error: (err) => this.fail(err) });
  }

  protected cancel(r: Reservation): void {
    if (!confirm('Annuler ce créneau ?')) return;
    this.api.cancelReservation(r.id).subscribe({ next: () => this.load(), error: (err) => this.fail(err) });
  }

  private fail(err: unknown): void {
    this.error.set(errorMessage(err));
  }
}
