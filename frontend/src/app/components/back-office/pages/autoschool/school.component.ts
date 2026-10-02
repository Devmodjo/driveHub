import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Observable } from 'rxjs';
import { DrivingSchoolService } from '../../../../services/school-service/driving-school.service';
import { RegistryStats } from '../../../../interfaces/RegistryStats';
import { SchoolRegistryDetail } from '../../../../interfaces/SchoolRegistryDetail';
import { ApiResponse } from '../../../../interfaces/ApiResponse';
import { errorMessage } from '../../../../shared/http-error';
import { RegistryKpiComponent } from '../../schoolregistry-component/registrykpi-component/registry.kpi.component';
import { RegistryChartComponent } from '../../schoolregistry-component/registry-chart-component/registry.chart.component';
import { RegistryTableComponent } from '../../schoolregistry-component/registry-table-component/registry.table.component';
import { BoConfirmComponent } from '../../shared/bo-confirm.component';
import { BoDrawerComponent } from '../../shared/bo-drawer.component';
import { BoToastComponent, BoToastMessage } from '../../shared/bo-toast.component';
import { schoolStatusClass, schoolStatusLabel } from '../../shared/bo-status';

/** Action sensible qui attend la confirmation de l'administrateur. */
interface PendingAction {
  kind: 'reject' | 'suspend' | 'delete';
  id: string;
}

/**
 * Page « Auto-écoles » du back-office (/backoffice/dashboard/schools).
 * Elle appelle l'API via DrivingSchoolService et distribue les données
 * aux sous-composants (indicateurs, graphiques, liste).
 */
@Component({
  selector: 'app-school',
  imports: [
    DatePipe, RegistryKpiComponent, RegistryChartComponent, RegistryTableComponent,
    BoConfirmComponent, BoDrawerComponent, BoToastComponent,
  ],
  templateUrl: './school.component.html',
})
export class SchoolComponent implements OnInit {
  private schoolService = inject(DrivingSchoolService);
  protected readonly statusLabel = schoolStatusLabel;
  protected readonly statusClass = schoolStatusClass;

  stats = signal<RegistryStats | null>(null);
  schools = signal<SchoolRegistryDetail[]>([]);
  isLoading = signal(false);

  currentPage = signal(0);
  pageSize = signal(10);
  totalPages = signal(0);
  totalElements = signal(0);
  statusFilter = signal<string | undefined>(undefined);

  actionMessage = signal<BoToastMessage | null>(null);

  selectedSchool = signal<SchoolRegistryDetail | null>(null);
  showDetailDrawer = signal(false);
  isLoadingDetail = signal(false);

  /** Action en attente de confirmation (rejet, suspension, suppression). */
  pendingAction = signal<PendingAction | null>(null);
  /** Vrai pendant l'appel à l'API d'une action confirmée. */
  isActing = signal(false);

  /** Pourcentage d'auto-écoles actives par rapport au total. */
  activePercent = computed(() => {
    const s = this.stats();
    if (!s || s.totalRegistries === 0) return '0 % du total';
    return ((s.activeRegistries / s.totalRegistries) * 100).toFixed(1) + ' % du total';
  });

  /** Textes de la fenêtre de confirmation selon l'action demandée. */
  confirmText = computed(() => {
    const action = this.pendingAction();
    const name = action ? this.nameOf(action.id) : '';
    switch (action?.kind) {
      case 'reject':
        return { title: 'Rejeter cette auto-école ?', message: `${name} sera informée du rejet de sa demande.`, label: 'Rejeter', tone: 'danger' as const };
      case 'suspend':
        return { title: 'Suspendre cette auto-école ?', message: `${name} ne pourra plus utiliser DriveHub jusqu'à sa réactivation.`, label: 'Suspendre', tone: 'warning' as const };
      default:
        return { title: 'Supprimer cette auto-école ?', message: `${name} sera supprimée définitivement. Cette action est irréversible.`, label: 'Supprimer', tone: 'danger' as const };
    }
  });

  ngOnInit(): void {
    this.loadStats();
    this.loadSchools();
  }

  /** Charge les statistiques globales depuis l'API. */
  loadStats(): void {
    this.schoolService.getSchoolRegistryStats().subscribe({
      next: (data) => this.stats.set(data),
      error: (err) => this.showMessage(errorMessage(err, 'Impossible de charger les statistiques.'), 'error'),
    });
  }

  /** Charge la liste paginée des auto-écoles en fonction du filtre actif. */
  loadSchools(): void {
    this.isLoading.set(true);
    this.schoolService
      .getAllRegistry(this.currentPage(), this.pageSize(), this.statusFilter())
      .subscribe({
        next: (response) => {
          this.schools.set(response.content);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.showMessage(errorMessage(err, 'Impossible de charger les auto-écoles.'), 'error');
        },
      });
  }

  /** Applique un filtre par statut et recharge depuis la première page. */
  onFilterChange(status: string | undefined): void {
    this.statusFilter.set(status);
    this.currentPage.set(0);
    this.loadSchools();
  }

  /** Change de page et recharge la liste. */
  onPageChange(page: number): void {
    this.currentPage.set(page);
    this.loadSchools();
  }

  /** Approuve une auto-école (action directe, sans confirmation). */
  onApprove(registryId: string): void {
    this.run(this.schoolService.approveRegistry(registryId), 'Auto-école approuvée.', 'Erreur lors de l\'approbation.');
  }

  /** Réactive une auto-école suspendue (même endpoint que l'approbation). */
  onReactivate(registryId: string): void {
    this.run(this.schoolService.approveRegistry(registryId), 'Auto-école réactivée.', 'Erreur lors de la réactivation.');
  }

  /** Les actions sensibles passent d'abord par la fenêtre de confirmation. */
  onReject(registryId: string): void {
    this.pendingAction.set({ kind: 'reject', id: registryId });
  }

  onSuspend(registryId: string): void {
    this.pendingAction.set({ kind: 'suspend', id: registryId });
  }

  onDelete(registryId: string): void {
    this.pendingAction.set({ kind: 'delete', id: registryId });
  }

  /** L'administrateur a confirmé : on appelle l'API correspondante. */
  confirmPendingAction(): void {
    const action = this.pendingAction();
    if (!action) return;
    const calls = {
      reject: () => this.run(this.schoolService.rejectRegistry(action.id), 'Auto-école rejetée.', 'Erreur lors du rejet.'),
      suspend: () => this.run(this.schoolService.suspendRegistry(action.id), 'Auto-école suspendue.', 'Erreur lors de la suspension.'),
      delete: () => this.run(this.schoolService.deleteRegistry(action.id), 'Auto-école supprimée.', 'Erreur lors de la suppression.'),
    };
    calls[action.kind]();
  }

  /** Charge le détail d'une auto-école et ouvre le volet latéral. */
  onViewDetail(registryId: string): void {
    this.showDetailDrawer.set(true);

    // On cherche d'abord dans la liste déjà chargée (évite un appel réseau)
    const fromList = this.schools().find((s) => s.id === registryId);
    if (fromList) {
      this.selectedSchool.set(fromList);
      return;
    }

    this.isLoadingDetail.set(true);
    this.schoolService.getSchoolRegistryDetail(registryId).subscribe({
      next: (response: any) => {
        // L'API peut renvoyer { data: {...} } ou directement l'objet
        this.selectedSchool.set(response?.data ?? response);
        this.isLoadingDetail.set(false);
      },
      error: (err) => {
        this.showMessage(errorMessage(err, 'Impossible de charger les détails.'), 'error');
        this.closeDrawer();
        this.isLoadingDetail.set(false);
      },
    });
  }

  /** Ferme le volet de détail. */
  closeDrawer(): void {
    this.showDetailDrawer.set(false);
    this.selectedSchool.set(null);
  }

  /**
   * Exécute une action de l'API puis rafraîchit la liste et les statistiques.
   * En cas d'erreur, le message précis du backend est affiché (ex. 403 : rôle insuffisant).
   */
  private run(call: Observable<ApiResponse>, success: string, failure: string): void {
    this.isActing.set(true);
    call.subscribe({
      next: () => {
        this.isActing.set(false);
        this.pendingAction.set(null);
        this.closeDrawer();
        this.showMessage(success, 'success');
        this.loadSchools();
        this.loadStats();
      },
      error: (err) => {
        this.isActing.set(false);
        this.pendingAction.set(null);
        this.showMessage(errorMessage(err, failure), 'error');
      },
    });
  }

  /** Nom d'une auto-école de la liste (pour les messages de confirmation). */
  private nameOf(id: string): string {
    return this.schools().find((s) => s.id === id)?.schoolName ?? this.selectedSchool()?.schoolName ?? 'Cette auto-école';
  }

  /** Affiche un message temporaire pendant 3,5 secondes. */
  private showMessage(text: string, type: 'success' | 'error'): void {
    this.actionMessage.set({ text, type });
    setTimeout(() => this.actionMessage.set(null), 3500);
  }
}
