import { Component, inject, signal } from '@angular/core';
import { PendingJoinRequest } from '../../../../interfaces/drivehub.models';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { errorMessage } from '../../../../shared/http-error';
import { formatDateTime, label } from '../../shared/labels';
import { PageHeaderComponent } from '../../shared/page-header.component';

/**
 * Demandes d'adhésion reçues par l'auto-école (schéma public).
 * Approuver copie le profil de l'élève / du moniteur dans le schéma de l'auto-école.
 */
@Component({
  selector: 'app-join-requests',
  imports: [PageHeaderComponent],
  template: `
    <app-page-header title="Demandes d'adhésion" subtitle="Élèves et moniteurs qui souhaitent rejoindre votre auto-école." />
    @if (message()) { <div class="alert-success mb-6">{{ message() }}</div> }
    @if (error()) { <div class="alert-error mb-6">{{ error() }}</div> }

    <div class="premium-card rounded-[24px] p-4 overflow-x-auto">
      <table class="data-table">
        <thead><tr><th>Nom</th><th>Email</th><th>Rôle</th><th>Date</th><th></th></tr></thead>
        <tbody>
          @for (r of requests(); track r.requestId) {
            <tr>
              <td class="font-semibold">{{ r.userName }}</td><td>{{ r.userEmail }}</td><td>{{ label(r.role) }}</td>
              <td>{{ formatDateTime(r.requestedAt) }}</td>
              <td class="whitespace-nowrap text-right space-x-2">
                <button class="btn-small bg-[#0070f3] text-white" [disabled]="busy() === r.requestId" (click)="approve(r)">Approuver</button>
                <button class="btn-small text-red-600 hover:bg-red-500/10" [disabled]="busy() === r.requestId" (click)="reject(r)">Refuser</button>
              </td>
            </tr>
          } @empty {
            <tr><td colspan="5" class="text-center text-black/50 dark:text-white/50">Aucune demande en attente.</td></tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class JoinRequestsComponent {
  private readonly api = inject(SchoolApiService);
  protected readonly label = label;
  protected readonly formatDateTime = formatDateTime;
  protected readonly requests = signal<PendingJoinRequest[]>([]);
  protected readonly busy = signal<string | null>(null);
  protected readonly message = signal('');
  protected readonly error = signal('');

  constructor() {
    this.load();
  }

  private load(): void {
    this.api.pendingJoinRequests(0, 100).subscribe({
      next: (page) => this.requests.set(page.content),
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  protected approve(r: PendingJoinRequest): void {
    this.act(r, this.api.approveJoin(r.requestId));
  }

  protected reject(r: PendingJoinRequest): void {
    this.act(r, this.api.rejectJoin(r.requestId));
  }

  private act(r: PendingJoinRequest, call: ReturnType<SchoolApiService['approveJoin']>): void {
    this.busy.set(r.requestId);
    this.error.set('');
    call.subscribe({
      next: (res) => { this.busy.set(null); this.message.set(`${r.userName} : ${res.message}`); this.load(); },
      error: (err) => { this.busy.set(null); this.error.set(errorMessage(err)); },
    });
  }
}
