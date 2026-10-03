import { Component, inject, signal } from '@angular/core';
import { LucideDynamicIcon } from '@lucide/angular';
import { Observable } from 'rxjs';
import { PendingJoinRequest, UserDocument } from '../../../../interfaces/drivehub.models';
import { DocumentService } from '../../../../services/document-service/document.service';
import { SchoolApiService } from '../../../../services/school-api-service/school-api.service';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { DocumentsDialogComponent } from '../../shared/documents-dialog.component';
import { formatDateTime, label } from '../../shared/labels';
import { PageHeaderComponent } from '../../shared/page-header.component';

/** Fenêtre des justificatifs ouverte pour une demande. */
interface DocumentsView {
  request: PendingJoinRequest;
  documents: Observable<UserDocument[]>;
  /** Téléchargement d'un fichier de cette demande. */
  loader: (doc: UserDocument) => Observable<Blob>;
}

/**
 * Demandes d'adhésion reçues par l'auto-école (schéma public).
 * Avant d'approuver un MONITEUR, le responsable vérifie ses justificatifs (pièce d'identité et CAPEC).
 * Aucun justificatif n'est demandé aux élèves : leur demande n'a donc pas de bouton « Justificatifs ».
 * Approuver copie le profil de l'élève / du moniteur dans le schéma de l'auto-école
 * (et, pour un moniteur, passe ses justificatifs à « Vérifié »).
 *
 * Mobile : une carte par demande ; à partir de md: un tableau.
 */
@Component({
  selector: 'app-join-requests',
  imports: [LucideDynamicIcon, PageHeaderComponent, DocumentsDialogComponent],
  template: `
    <app-page-header title="Demandes d'adhésion" subtitle="Élèves et moniteurs qui souhaitent rejoindre votre auto-école. Pour un moniteur, vérifiez ses justificatifs avant d'approuver." />
    @if (message()) { <div class="alert-success mb-6">{{ message() }}</div> }
    @if (error()) { <div class="alert-error mb-6">{{ error() }}</div> }

    <!-- Mobile : cartes -->
    <ul class="md:hidden space-y-3">
      @for (r of requests(); track r.requestId) {
        <li class="premium-card rounded-[20px] p-4">
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <p class="font-bold truncate">{{ r.userName }}</p>
              <p class="text-sm text-black/50 dark:text-white/50 break-all">{{ r.userEmail }}</p>
            </div>
            <span class="badge shrink-0 bg-[#0070f3]/10 text-[#0070f3]">{{ label(r.role) }}</span>
          </div>
          <p class="mt-2 text-xs text-black/50 dark:text-white/50">Demande du {{ formatDateTime(r.requestedAt) }}</p>
          @if (r.role === 'MONITOR') {
            <button type="button" class="mt-3 btn-small min-h-11 w-full bg-black/5 dark:bg-white/10" (click)="showDocuments(r)">
              <svg [lucideIcon]="icons.FileText" [size]="15" /> Voir les justificatifs
            </button>
          }
          <div class="mt-2 grid grid-cols-2 gap-2">
            <button class="btn-small min-h-11 bg-[#0070f3] text-white" [disabled]="busy() === r.requestId" (click)="approve(r)">Approuver</button>
            <button class="btn-small min-h-11 text-red-600 border border-red-500/20 hover:bg-red-500/10" [disabled]="busy() === r.requestId" (click)="reject(r)">Refuser</button>
          </div>
        </li>
      } @empty {
        <li class="premium-card rounded-[20px] p-6 text-center text-sm text-black/50 dark:text-white/50">Aucune demande en attente.</li>
      }
    </ul>

    <!-- Écrans moyens et grands : tableau -->
    <div class="hidden md:block premium-card rounded-[24px] p-4 overflow-x-auto">
      <table class="data-table">
        <thead><tr><th>Nom</th><th>Email</th><th>Rôle</th><th>Date</th><th></th></tr></thead>
        <tbody>
          @for (r of requests(); track r.requestId) {
            <tr>
              <td class="font-semibold">{{ r.userName }}</td><td>{{ r.userEmail }}</td><td>{{ label(r.role) }}</td>
              <td>{{ formatDateTime(r.requestedAt) }}</td>
              <td class="whitespace-nowrap text-right space-x-2">
                @if (r.role === 'MONITOR') {
                  <button class="btn-small hover:bg-black/5 dark:hover:bg-white/5" (click)="showDocuments(r)">
                    <svg [lucideIcon]="icons.FileText" [size]="15" /> Justificatifs
                  </button>
                }
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

    @if (viewing(); as v) {
      <app-documents-dialog [title]="'Justificatifs de ' + v.request.userName"
                            [subtitle]="label(v.request.role) + ' · ' + v.request.userEmail"
                            [documents]="v.documents" [expected]="['CNI', 'CAPEC']" [fileLoader]="v.loader"
                            (closed)="viewing.set(null)" />
    }
  `,
})
export class JoinRequestsComponent {
  protected readonly icons = ICONS;
  private readonly api = inject(SchoolApiService);
  private readonly documentService = inject(DocumentService);
  protected readonly label = label;
  protected readonly formatDateTime = formatDateTime;
  protected readonly requests = signal<PendingJoinRequest[]>([]);
  protected readonly busy = signal<string | null>(null);
  protected readonly message = signal('');
  protected readonly error = signal('');
  protected readonly viewing = signal<DocumentsView | null>(null);

  constructor() {
    this.load();
  }

  private load(): void {
    this.api.pendingJoinRequests(0, 100).subscribe({
      next: (page) => this.requests.set(page.content),
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  /** Ouvre la fenêtre des justificatifs d'un moniteur (pièce d'identité et CAPEC). */
  protected showDocuments(r: PendingJoinRequest): void {
    this.viewing.set({
      request: r,
      documents: this.documentService.joinRequestDocuments(r.requestId),
      loader: (doc) => this.documentService.joinRequestFile(r.requestId, doc.id),
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
