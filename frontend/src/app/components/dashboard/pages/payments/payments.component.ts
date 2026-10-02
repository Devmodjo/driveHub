import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { Payment, PaymentMethod, PaymentMotif, PaymentSummary, Student } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { formatAmount, formatDate, label } from '../../shared/labels';
import { PageHeaderComponent } from '../../shared/page-header.component';
import { StatusBadgeComponent } from '../../shared/status-badge.component';

/**
 * Paiements.
 *  - Élève : paiement Mobile Money (MTN MoMo / Orange Money) via Campay. Le backend envoie la demande
 *    à Campay, l'élève valide sur son téléphone, puis le webhook Campay (ou le bouton "Actualiser")
 *    fait passer le paiement à "Validé" ou "Rejeté".
 *  - Moniteur : suivi des encaissements, saisie des paiements reçus à la caisse, validation manuelle.
 */
@Component({
  selector: 'app-payments',
  imports: [FormsModule, LucideDynamicIcon, PageHeaderComponent, StatusBadgeComponent],
  template: `
    <app-page-header title="Paiements" [subtitle]="isMonitor ? 'Encaissements de votre auto-école.' : 'Réglez vos frais par Mobile Money.'">
      <button class="btn-primary" (click)="formOpen.set(!formOpen())">
        <svg [lucideIcon]="icons.CreditCard" [size]="16" /> {{ isMonitor ? 'Enregistrer un paiement' : 'Payer' }}
      </button>
    </app-page-header>
    @if (info()) { <div class="alert-success mb-6">{{ info() }}</div> }
    @if (error()) { <div class="alert-error mb-6">{{ error() }}</div> }

    @if (summary(); as s) {
      <div class="grid grid-cols-1 md:grid-cols-2 gap-5 mb-8">
        <div class="premium-card rounded-[24px] p-6"><p class="field-label">Encaissé</p><p class="text-3xl font-black">{{ formatAmount(s.totalValidated) }}</p></div>
        <div class="premium-card rounded-[24px] p-6"><p class="field-label">En attente</p><p class="text-3xl font-black">{{ formatAmount(s.totalPending) }}</p></div>
      </div>
    }

    @if (formOpen()) {
      <form class="premium-card rounded-[24px] p-6 mb-8 grid grid-cols-1 md:grid-cols-3 gap-4 items-end" (ngSubmit)="create()">
        @if (isMonitor) {
          <div><label class="field-label" for="student">Élève</label>
            <select id="student" class="field-input" name="student" required [(ngModel)]="form.studentsId">
              <option value="">Choisir...</option>
              @for (s of students(); track s.id) { <option [value]="s.id">{{ s.firstname }} {{ s.lastname }}</option> }
            </select></div>
        }
        <div><label class="field-label" for="motif">Motif</label>
          <select id="motif" class="field-input" name="motif" [(ngModel)]="form.motif">
            <option value="INSCRIPTION">Inscription</option><option value="EXAMS">Examen</option>
          </select></div>
        <div><label class="field-label" for="amount">Montant (FCFA)</label>
          <input id="amount" class="field-input" type="number" min="1" step="1" name="amount" required [(ngModel)]="form.amount" /></div>
        <div><label class="field-label" for="method">Moyen de paiement</label>
          <select id="method" class="field-input" name="method" [(ngModel)]="form.method">
            <option value="MOMO">MTN Mobile Money</option><option value="OM">Orange Money</option>
            @if (isMonitor) { <option value="CASH">Espèces</option> }
          </select></div>
        @if (!isMonitor) {
          <div><label class="field-label" for="phone">Numéro Mobile Money</label>
            <input id="phone" class="field-input" name="phone" placeholder="6XX XX XX XX" required [(ngModel)]="form.phoneNumber" /></div>
        }
        <button type="submit" class="btn-primary" [disabled]="sending()">{{ sending() ? 'Envoi...' : 'Valider' }}</button>
      </form>
    }

    <div class="premium-card rounded-[24px] p-4 overflow-x-auto">
      <table class="data-table">
        <thead><tr><th>Date</th>@if (isMonitor) {<th>Élève</th>}<th>Motif</th><th>Montant</th><th>Moyen</th><th>Statut</th><th></th></tr></thead>
        <tbody>
          @for (p of payments(); track p.id) {
            <tr>
              <td>{{ formatDate(p.datePayment) }}</td>
              @if (isMonitor) { <td>{{ p.studentFirstname }} {{ p.studentLastname }}</td> }
              <td>{{ label(p.motif) }}</td><td class="font-semibold whitespace-nowrap">{{ formatAmount(p.amount) }}</td>
              <td>{{ label(p.method) }}</td>
              <td><app-status-badge [value]="p.paymentStatus" />
                @if (p.gatewayMessage && p.paymentStatus !== 'VALIDATE') { <p class="text-xs text-black/40 dark:text-white/40 mt-1">{{ p.gatewayMessage }}</p> }</td>
              <td class="text-right whitespace-nowrap space-x-2">
                @if (p.paymentStatus === 'PENDING') {
                  @if (p.provider !== 'MANUAL') {
                    <button class="btn-small hover:bg-black/5 dark:hover:bg-white/5" (click)="refresh(p)" aria-label="Actualiser"><svg [lucideIcon]="icons.RefreshCw" [size]="15" /> Actualiser</button>
                  }
                  @if (isMonitor) {
                    <button class="btn-small bg-[#0070f3] text-white" (click)="validate(p)">Valider</button>
                    <button class="btn-small text-red-600 hover:bg-red-500/10" (click)="reject(p)">Rejeter</button>
                  }
                }
              </td>
            </tr>
          } @empty {
            <tr><td [attr.colspan]="isMonitor ? 7 : 6" class="text-center text-black/50 dark:text-white/50">Aucun paiement.</td></tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class PaymentsComponent {
  protected readonly icons = ICONS;
  protected readonly label = label;
  protected readonly formatDate = formatDate;
  protected readonly formatAmount = formatAmount;
  private readonly api = inject(SchoolApiService);
  protected readonly isMonitor = inject(SessionService).isMonitor();

  protected readonly payments = signal<Payment[]>([]);
  protected readonly students = signal<Student[]>([]);
  protected readonly summary = signal<PaymentSummary | null>(null);
  protected readonly formOpen = signal(false);
  protected readonly sending = signal(false);
  protected readonly info = signal('');
  protected readonly error = signal('');
  protected form = {
    studentsId: '', amount: 25000, method: (this.isMonitor ? 'CASH' : 'MOMO') as PaymentMethod,
    motif: 'INSCRIPTION' as PaymentMotif, phoneNumber: '',
  };

  constructor() {
    this.load();
    if (this.isMonitor) {
      this.api.students(0, 500).subscribe({ next: (p) => this.students.set(p.content) });
    }
  }

  private load(): void {
    this.api.payments(0, 200).subscribe({ next: (p) => this.payments.set(p.content), error: (err) => this.fail(err) });
    if (this.isMonitor) {
      this.api.paymentSummary().subscribe({ next: (s) => this.summary.set(s) });
    }
  }

  protected create(): void {
    this.sending.set(true);
    this.error.set('');
    this.info.set('');
    this.api.createPayment({
      studentsId: this.isMonitor ? this.form.studentsId : null,
      amount: Number(this.form.amount),
      method: this.form.method,
      motif: this.form.motif,
      phoneNumber: this.isMonitor ? null : this.form.phoneNumber.trim(),
    }).subscribe({
      next: (p) => {
        this.sending.set(false);
        this.formOpen.set(false);
        this.info.set(p.paymentStatus === 'PENDING'
          ? 'Demande envoyée : confirmez le paiement sur votre téléphone, puis cliquez sur « Actualiser ».'
          : 'Paiement enregistré.');
        this.load();
      },
      error: (err) => { this.sending.set(false); this.fail(err); },
    });
  }

  protected refresh(p: Payment): void {
    this.api.refreshPayment(p.id).subscribe({ next: () => this.load(), error: (err) => this.fail(err) });
  }

  protected validate(p: Payment): void {
    this.api.validatePayment(p.id).subscribe({ next: () => this.load(), error: (err) => this.fail(err) });
  }

  protected reject(p: Payment): void {
    if (!confirm('Rejeter ce paiement ?')) return;
    this.api.rejectPayment(p.id).subscribe({ next: () => this.load(), error: (err) => this.fail(err) });
  }

  private fail(err: unknown): void {
    this.error.set(errorMessage(err));
  }
}
