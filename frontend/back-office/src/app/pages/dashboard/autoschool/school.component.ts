import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { DrivingSchoolService } from '../../../services/school-service/driving-school.service';
import { RegistryStats } from '../../../interfaces/RegistryStats';
//import { SchoolRegistryDetail } from '../../../interfaces/SchoolRegistryDetail';
import { SchoolRegistryDetail } from '../../../interfaces/SchoolRegistryDetail';
import { RegistryKpiComponent } from '../../../components/schoolregistry-component/registrykpi-component/registry.kpi.component';
import { RegistryChartComponent } from '../../../components/schoolregistry-component/registry-chart-component/registry.chart.component';
import { RegistryTableComponent } from '../../../components/schoolregistry-component/registry-table-component/registry.table.component';

/**
 * Page principale "Auto-ecoles" du dashboard admin.
 * Orchestre les appels API via DrivingSchoolService et distribue
 * les donnees aux sous-composants (KPI, charts, table).
 */
@Component({
  selector: 'app-school',
  imports: [DatePipe, RegistryKpiComponent, RegistryChartComponent, RegistryTableComponent],
  templateUrl: './school.component.html',
  styleUrl: './school.component.css',
})
export class SchoolComponent implements OnInit {

  private schoolService = inject(DrivingSchoolService);

  stats = signal<RegistryStats | null>(null);
  schools = signal<SchoolRegistryDetail[]>([]);
  isLoading = signal(false);

  currentPage = signal(0);
  pageSize = signal(10);
  totalPages = signal(0);
  totalElements = signal(0);
  statusFilter = signal<string | undefined>(undefined);

  actionMessage = signal<{ text: string; type: 'success' | 'error' } | null>(null);

  selectedSchool = signal<SchoolRegistryDetail | null>(null);
  showDetailDrawer = signal(false);
  isLoadingDetail = signal(false);

  /** Pourcentage d'ecoles actives par rapport au total. */
  activePercent = computed(() => {
    const s = this.stats();
    if (!s || s.totalRegistries === 0) return '0%';
    return ((s.activeRegistries / s.totalRegistries) * 100).toFixed(1) + '% du total';
  });

  ngOnInit(): void {
    this.loadStats();
    this.loadSchools();
  }

  /** Charge les statistiques globales depuis l'API. */
  loadStats(): void {
    this.schoolService.getSchoolRegistryStats().subscribe({
      next: (data) => this.stats.set(data),
      error: () => this.showMessage('Impossible de charger les statistiques', 'error'),
    });
  }

  /** Charge la liste paginee des auto-ecoles en fonction des filtres actifs. */
  loadSchools(): void {
    this.isLoading.set(true);
    this.schoolService
      .getAllRegistry(this.currentPage(), this.pageSize(), this.statusFilter())
      .subscribe({
        next: (response) => {
          this.schools.set(response.content);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);
        },
        error: () => this.showMessage('Impossible de charger les auto-ecoles', 'error'),
        complete: () => this.isLoading.set(false),
      });
  }

  /** Applique un filtre par statut et recharge depuis la premiere page. */
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

  /** Approuve une auto-ecole et rafraichit la liste et les stats. */
  onApprove(registryId: string): void {
    this.schoolService.approveRegistry(registryId).subscribe({
      next: () => {
        this.showMessage('Auto-ecole approuvee avec succes', 'success');
        this.loadSchools();
        this.loadStats();
        this.closeDrawer();
      },
      error: () => this.showMessage('Erreur lors de l\'approbation', 'error'),
    });
  }

  /** Rejette une auto-ecole et rafraichit la liste et les stats. */
  onReject(registryId: string): void {
    this.schoolService.rejectRegistry(registryId).subscribe({
      next: () => {
        this.showMessage('Auto-ecole rejetee', 'success');
        this.loadSchools();
        this.loadStats();
        this.closeDrawer();
      },
      error: () => this.showMessage('Erreur lors du rejet', 'error'),
    });
  }

  /** Supprime une auto-ecole apres confirmation utilisateur. */
  onDelete(registryId: string): void {
    const confirmed = confirm('Supprimer cette auto-ecole ? Cette action est irreversible.');
    if (!confirmed) return;

    this.schoolService.deleteRegistry(registryId).subscribe({
      next: () => {
        this.showMessage('Auto-ecole supprimee', 'success');
        this.loadSchools();
        this.loadStats();
        if (this.selectedSchool()?.id === registryId) {
          this.closeDrawer();
        }
      },
      error: () => this.showMessage('Erreur lors de la suppression', 'error'),
    });
  }

  /** Suspend une auto-ecole. */
  onSuspend(registryId: string): void {
    const confirmed = confirm('Voulez-vous vraiment suspendre cette auto-ecole ?');
    if (!confirmed) return;

    this.schoolService.suspendRegistry(registryId).subscribe({
      next: () => {
        this.showMessage('Auto-ecole suspendue', 'success');
        this.loadSchools();
        this.loadStats();
        this.closeDrawer();
      },
      error: () => this.showMessage('Erreur lors de la suspension', 'error'),
    });
  }

  /** Reactive une auto-ecole. */
  onReactivate(registryId: string): void {
    this.schoolService.approveRegistry(registryId).subscribe({
      next: () => {
        this.showMessage('Auto-ecole activee', 'success');
        this.loadSchools();
        this.loadStats();
        this.closeDrawer();
      },
      error: () => this.showMessage('Erreur lors de l\'activation', 'error'),
    });
  }

  /** Charge le detail complet d'une auto-ecole et ouvre le drawer lateral. */
  onViewDetail(registryId: string): void {
    this.isLoadingDetail.set(true);
    this.showDetailDrawer.set(true);

    // Tente d'abord de trouver l'ecole dans la liste deja chargee
    const fromList = this.schools().find(s => s.id === registryId);
    if (fromList) {
      this.selectedSchool.set(fromList);
      this.isLoadingDetail.set(false);
      return;
    }

    // Sinon, appel API
    this.schoolService.getSchoolRegistryDetail(registryId).subscribe({
      next: (response: any) => {
        // Gere le cas ou l'API retourne { data: {...} } ou directement l'objet
        const detail = response?.data ?? response;
        this.selectedSchool.set(detail);
        this.isLoadingDetail.set(false);
      },
      error: () => {
        this.showMessage('Impossible de charger les details', 'error');
        this.showDetailDrawer.set(false);
        this.isLoadingDetail.set(false);
      },
    });
  }

  /** Ferme le drawer de detail et reinitialise la selection. */
  closeDrawer(): void {
    this.showDetailDrawer.set(false);
    this.selectedSchool.set(null);
  }

  /** Retourne un libelle lisible pour le statut d'une auto-ecole. */
  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      ACTIVE: 'Active',
      APPROVED: 'Approuvee',
      PENDING: 'En attente',
      REJECTED: 'Rejetee',
      SUSPENDED: 'Suspendue',
      INACTIVE: 'Inactive',
    };
    return labels[status] ?? status;
  }

  /** Retourne la classe CSS associee au statut pour le badge colore. */
  getStatusClass(status: string): string {
    const classes: Record<string, string> = {
      ACTIVE: 'badge badge-green',
      APPROVED: 'badge badge-green',
      PENDING: 'badge badge-amber',
      REJECTED: 'badge badge-red',
      SUSPENDED: 'badge badge-gray',
      INACTIVE: 'badge badge-gray',
    };
    return classes[status] ?? 'badge';
  }

  /** Affiche un message toast temporaire pendant 3.5 secondes. */
  private showMessage(text: string, type: 'success' | 'error'): void {
    this.actionMessage.set({ text, type });
    setTimeout(() => this.actionMessage.set(null), 3500);
  }
}