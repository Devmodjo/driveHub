import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';

// Services : communication avec l'API
import { AdminService } from '../../../../services/admin-service/admin.service';
import { DrivingSchoolService } from '../../../../services/school-service/driving-school.service';

// Interfaces : modèles de données
import { AdminStats } from '../../../../interfaces/AdminStats';
import { ActiveDrivingSchool } from '../../../../interfaces/ActiveDrivingSchool';
import { AdminProfile } from '../../../../interfaces/AdminProfile';
import { PendingSchoolRequest } from '../../../../interfaces/PendingSchoolRequest';

import { errorMessage } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { DashboardKpiComponent } from '../../dashboard-component/dashdoard-kpi/dashboard.kpi.component';
import { BoToastComponent, BoToastMessage } from '../../shared/bo-toast.component';
import { initials } from '../../shared/bo-status';

/**
 * Vue d'ensemble du back-office (/backoffice/dashboard/overview) :
 * indicateurs clés, demandes à traiter (admins et auto-écoles) et auto-écoles actives.
 *
 * Certaines données sont réservées à certains rôles (ex. la liste des admins en attente est
 * réservée aux ROOT) : en cas de refus de l'API, le bloc concerné reste simplement vide.
 */
@Component({
  selector: 'app-overview',
  imports: [DashboardKpiComponent, RouterLink, LucideDynamicIcon, BoToastComponent],
  templateUrl: './overview.component.html',
})
export class OverviewComponent implements OnInit {
  private adminService = inject(AdminService);
  private drivingSchoolService = inject(DrivingSchoolService);
  protected readonly icons = ICONS;

  adminstats = signal<AdminStats | null>(null);
  countActiveSchool = signal<number>(0);
  activeSchools = signal<ActiveDrivingSchool[]>([]);
  pendingSchools = signal<PendingSchoolRequest[]>([]);
  countPendingSchoolRequest = signal<number>(0);
  pendingAdmins = signal<AdminProfile[]>([]);
  actionMessage = signal<BoToastMessage | null>(null);
  /** Identifiant de l'élément en cours de traitement (désactive son bouton). */
  busyId = signal<string | null>(null);

  ngOnInit(): void {
    this.loadData();
  }

  /** Charge toutes les données ; chaque appel est indépendant et met à jour son propre signal. */
  loadData(): void {
    this.adminService.getAdminStats().subscribe({
      next: (res) => this.adminstats.set(res),
      error: () => this.adminstats.set(null),
    });

    this.adminService.getAdminsPendingRequest().subscribe({
      next: (res) => this.pendingAdmins.set(res ?? []),
      error: () => this.pendingAdmins.set([]),
    });

    this.drivingSchoolService.pendingSchoolRequest().subscribe({
      next: (res) => {
        this.pendingSchools.set(res ?? []);
        this.countPendingSchoolRequest.set(res?.length ?? 0);
      },
      error: () => this.pendingSchools.set([]),
    });

    this.drivingSchoolService.activeDrivingSchool().subscribe({
      next: (res) => {
        this.activeSchools.set(res ?? []);
        this.countActiveSchool.set(res?.length ?? 0);
      },
      error: () => this.activeSchools.set([]),
    });
  }

  /** Active un admin en attente. */
  activateAdmin(id: string): void {
    this.busyId.set(id);
    this.adminService.activateAdmin(id).subscribe({
      next: () => {
        this.busyId.set(null);
        this.showMessage('Administrateur activé.', 'success');
        this.loadData();
      },
      error: (err) => {
        this.busyId.set(null);
        this.showMessage(errorMessage(err, 'Erreur lors de l\'activation.'), 'error');
      },
    });
  }

  /** Approuve la demande d'une auto-école. */
  approveSchool(id: string): void {
    this.busyId.set(id);
    this.drivingSchoolService.approveDrivingSchooleRequest(id).subscribe({
      next: () => {
        this.busyId.set(null);
        this.showMessage('Auto-école approuvée.', 'success');
        this.loadData();
      },
      error: (err) => {
        this.busyId.set(null);
        this.showMessage(errorMessage(err, 'Erreur lors de l\'approbation.'), 'error');
      },
    });
  }

  /** Initiales d'un nom (ex. « Jean Dupont » → « JD ») pour les avatars. */
  getInitials(name: string): string {
    return initials(name);
  }

  /** Affiche un message temporaire pendant 3,5 secondes. */
  private showMessage(text: string, type: 'success' | 'error'): void {
    this.actionMessage.set({ text, type });
    setTimeout(() => this.actionMessage.set(null), 3500);
  }
}
