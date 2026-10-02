import { Component, inject, OnInit, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { LucideDynamicIcon } from '@lucide/angular';
import { AdminService } from '../../../../services/admin-service/admin.service';
import { DrivingSchoolService } from '../../../../services/school-service/driving-school.service';
import { AdminProfile } from '../../../../interfaces/AdminProfile';
import { ApiResponse } from '../../../../interfaces/ApiResponse';
import { PendingSchoolRequest } from '../../../../interfaces/PendingSchoolRequest';
import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { BoConfirmComponent } from '../../shared/bo-confirm.component';
import { BoDrawerComponent } from '../../shared/bo-drawer.component';
import { BoRegistryDocumentsComponent } from '../../shared/bo-registry-documents.component';
import { BoToastComponent, BoToastMessage } from '../../shared/bo-toast.component';
import { initials } from '../../shared/bo-status';

/**
 * Page « Demandes en attente » (/backoffice/dashboard/pending) : tout ce qui attend une décision.
 *  - demandes de création d'auto-école (justificatifs du responsable, approuver / rejeter) ;
 *  - comptes administrateurs à activer (ROOT uniquement : pour les autres rôles, l'API refuse
 *    et la section affiche un message explicite).
 */
@Component({
  selector: 'app-pending',
  imports: [LucideDynamicIcon, BoConfirmComponent, BoToastComponent, BoDrawerComponent, BoRegistryDocumentsComponent],
  template: `
    <bo-toast [message]="actionMessage()" />

    <header class="mb-6">
      <h1 class="bo-page-title">Demandes en attente</h1>
      <p class="bo-page-subtitle">Auto-écoles et administrateurs qui attendent une décision.</p>
    </header>

    <!-- Auto-écoles -->
    <section class="bo-card mb-5">
      <div class="flex items-center justify-between gap-3 px-4 sm:px-5 py-4 border-b border-black/5 dark:border-white/10">
        <h2 class="bo-section-title">Auto-écoles</h2>
        <span class="badge bg-amber-500/15 text-amber-700 dark:text-amber-300">{{ schools().length }}</span>
      </div>
      @if (loadingSchools()) {
        <p class="px-5 py-10 text-center text-sm text-black/50 dark:text-white/50">Chargement...</p>
      } @else if (schoolsError()) {
        <p class="px-5 py-6 text-sm text-red-600 dark:text-red-400">{{ schoolsError() }}</p>
      } @else if (schools().length === 0) {
        <p class="px-5 py-10 text-center text-sm text-black/50 dark:text-white/50">Aucune demande d'auto-école en attente.</p>
      } @else {
        <ul class="divide-y divide-black/5 dark:divide-white/10">
          @for (school of schools(); track school.id) {
            <li class="p-4 sm:px-5 flex flex-col md:flex-row md:items-center gap-3">
              <div class="min-w-0 grow">
                <p class="font-bold text-black dark:text-white">{{ school.schoolName }}</p>
                <p class="text-sm text-black/50 dark:text-white/50">
                  {{ school.city || 'Ville non renseignée' }}@if (school.country) {, {{ school.country }}}
                  @if (school.address) { · {{ school.address }} }
                </p>
                <p class="text-sm text-black/60 dark:text-white/60 mt-1">
                  Responsable : <span class="font-semibold">{{ school.monitorName || '—' }}</span>
                  @if (school.whatsappNumber) { · WhatsApp {{ school.whatsappNumber }} }
                </p>
              </div>
              <div class="grid grid-cols-2 md:flex gap-2 shrink-0">
                <button type="button" class="bo-btn bo-btn-outline col-span-2" (click)="documentsOf.set(school)">
                  <svg [lucideIcon]="icons.FileText" [size]="15" /> Justificatifs
                </button>
                <button type="button" class="bo-btn bo-btn-green" [disabled]="busyId() === school.id" (click)="approveSchool(school.id)">Approuver</button>
                <button type="button" class="bo-btn bo-btn-outline text-red-600! dark:text-red-400!" [disabled]="busyId() === school.id"
                        (click)="rejectTarget.set(school)">Rejeter</button>
              </div>
            </li>
          }
        </ul>
      }
    </section>

    <!-- Administrateurs -->
    <section class="bo-card">
      <div class="flex items-center justify-between gap-3 px-4 sm:px-5 py-4 border-b border-black/5 dark:border-white/10">
        <h2 class="bo-section-title">Administrateurs</h2>
        <span class="badge bg-[#0070f3]/10 text-[#0070f3]">{{ admins().length }}</span>
      </div>
      @if (loadingAdmins()) {
        <p class="px-5 py-10 text-center text-sm text-black/50 dark:text-white/50">Chargement...</p>
      } @else if (adminsError()) {
        <p class="px-5 py-6 text-sm text-black/60 dark:text-white/60 flex items-start gap-2">
          <svg [lucideIcon]="icons.Info" [size]="18" class="shrink-0 mt-0.5" /> {{ adminsError() }}
        </p>
      } @else if (admins().length === 0) {
        <p class="px-5 py-10 text-center text-sm text-black/50 dark:text-white/50">Aucun administrateur en attente.</p>
      } @else {
        <ul class="divide-y divide-black/5 dark:divide-white/10">
          @for (admin of admins(); track admin.id) {
            <li class="p-4 sm:px-5 flex items-center gap-3">
              <div class="w-10 h-10 shrink-0 rounded-xl bg-[#0070f3]/10 text-[#0070f3] flex items-center justify-center text-sm font-black">{{ initials(admin.name) }}</div>
              <div class="min-w-0 grow">
                <p class="font-bold text-sm text-black dark:text-white truncate">{{ admin.name }}</p>
                <p class="text-xs text-black/50 dark:text-white/50 truncate">{{ admin.email }} · {{ admin.role }}</p>
              </div>
              <button type="button" class="bo-btn bo-btn-green shrink-0" [disabled]="busyId() === admin.id" (click)="activateAdmin(admin.id)">Activer</button>
            </li>
          }
        </ul>
      }
    </section>

    @if (documentsOf(); as school) {
      <bo-drawer [title]="'Justificatifs · ' + school.schoolName" (closed)="documentsOf.set(null)">
        <p class="mb-4 text-sm text-black/60 dark:text-white/60">
          Responsable : <span class="font-semibold text-black dark:text-white">{{ school.monitorName || '—' }}</span>.
          Vérifiez sa pièce d'identité et son CAPEC avant d'approuver l'auto-école.
        </p>
        <bo-registry-documents [registryId]="school.id" (notify)="showMessage($event.text, $event.type)" />
        <div class="mt-6 grid grid-cols-2 gap-2">
          <button type="button" class="bo-btn bo-btn-green" [disabled]="busyId() === school.id" (click)="documentsOf.set(null); approveSchool(school.id)">Approuver</button>
          <button type="button" class="bo-btn bo-btn-outline text-red-600! dark:text-red-400!" [disabled]="busyId() === school.id"
                  (click)="documentsOf.set(null); rejectTarget.set(school)">Rejeter</button>
        </div>
      </bo-drawer>
    }

    @if (rejectTarget(); as target) {
      <bo-confirm title="Rejeter cette demande ?" [message]="target.schoolName + ' sera informée du rejet de sa demande.'"
                  confirmLabel="Rejeter" tone="danger" [busy]="busyId() === target.id"
                  (confirmed)="rejectSchool(target.id)" (cancelled)="rejectTarget.set(null)" />
    }
  `,
})
export class PendingComponent implements OnInit {
  private readonly schoolService = inject(DrivingSchoolService);
  private readonly adminService = inject(AdminService);
  protected readonly icons = ICONS;
  protected readonly initials = initials;

  protected readonly schools = signal<PendingSchoolRequest[]>([]);
  protected readonly admins = signal<AdminProfile[]>([]);
  protected readonly loadingSchools = signal(true);
  protected readonly loadingAdmins = signal(true);
  protected readonly schoolsError = signal('');
  protected readonly adminsError = signal('');
  protected readonly busyId = signal<string | null>(null);
  protected readonly rejectTarget = signal<PendingSchoolRequest | null>(null);
  /** Demande dont les justificatifs sont affichés dans le volet latéral. */
  protected readonly documentsOf = signal<PendingSchoolRequest | null>(null);
  protected readonly actionMessage = signal<BoToastMessage | null>(null);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.schoolService.pendingSchoolRequest().subscribe({
      next: (res) => { this.schools.set(res ?? []); this.loadingSchools.set(false); },
      error: (err) => {
        this.loadingSchools.set(false);
        this.schoolsError.set(errorMessage(err, 'Impossible de charger les demandes d\'auto-école.'));
      },
    });
    this.adminService.getAdminsPendingRequest().subscribe({
      next: (res) => { this.admins.set(res ?? []); this.loadingAdmins.set(false); },
      error: (err) => {
        this.loadingAdmins.set(false);
        // 403 : seuls les ROOT valident les administrateurs
        this.adminsError.set(err?.status === 403
          ? 'Seuls les administrateurs ROOT peuvent valider les comptes administrateurs.'
          : errorMessage(err, 'Impossible de charger les administrateurs en attente.'));
      },
    });
  }

  approveSchool(id: string): void {
    this.run(id, this.schoolService.approveRegistry(id), 'Auto-école approuvée.', 'Erreur lors de l\'approbation.');
  }

  rejectSchool(id: string): void {
    this.run(id, this.schoolService.rejectRegistry(id), 'Demande rejetée.', 'Erreur lors du rejet.');
  }

  activateAdmin(id: string): void {
    this.run(id, this.adminService.activateAdmin(id), 'Administrateur activé.', 'Erreur lors de l\'activation.');
  }

  /** Exécute une action puis recharge les deux listes. */
  private run(id: string, call: Observable<ApiResponse>, success: string, failure: string): void {
    this.busyId.set(id);
    call.subscribe({
      next: () => {
        this.busyId.set(null);
        this.rejectTarget.set(null);
        this.showMessage(success, 'success');
        this.load();
      },
      error: (err) => {
        this.busyId.set(null);
        this.rejectTarget.set(null);
        this.showMessage(errorMessage(err, failure), 'error');
      },
    });
  }

  protected showMessage(text: string, type: 'success' | 'error'): void {
    this.actionMessage.set({ text, type });
    setTimeout(() => this.actionMessage.set(null), 3500);
  }
}
