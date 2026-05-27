import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AdminService } from '../../../services/admin-service/admin.service';
import { AdminProfile } from '../../../interfaces/AdminProfile';
import AdminStatus from '../../../enums/adminstatus.enum';

/**
 * Page de gestion des administrateurs.
 * Route : /dashboard/admins
 *
 * Permet de :
 * - Visualiser tous les admins via l'API paginée GET /admin
 * - Naviguer de page en page
 * - Filtrer par statut (ACTIVE, PENDING, INACTIVE) ou par rôle
 * - Activer un admin en attente
 * - Supprimer un admin
 */
@Component({
  selector: 'app-admins',
  imports: [DatePipe, FormsModule],
  providers: [AdminService],
  templateUrl: './admins.component.html',
  styleUrl: './admins.component.css',
})
export class AdminsComponent implements OnInit {

  // --- Service ---

  private adminService = inject(AdminService);

  // --- Signaux : données de la page ---

  /** Liste des admins de la page courante (renvoyée par l'API) */
  admins = signal<AdminProfile[]>([]);

  /** Numéro de la page courante (commence à 0 côté API) */
  currentPage = signal<number>(0);

  /** Nombre d'éléments par page */
  pageSize = signal<number>(10);

  /** Nombre total de pages disponibles (fourni par l'API) */
  totalPages = signal<number>(0);

  /** Nombre total d'admins sur toutes les pages (fourni par l'API) */
  totalElements = signal<number>(0);

  /** Filtre actif par statut : undefined = tous */
  statusFilter = signal<string | undefined>(undefined);

  /** Filtre actif par rôle : undefined = tous */
  roleFilter = signal<string | undefined>(undefined);

  /** Message toast affiché après une action (succès ou erreur) */
  actionMessage = signal<{ text: string; type: 'success' | 'error' } | null>(null);

  /** Vrai pendant un appel API (désactive les boutons) */
  isLoading = signal<boolean>(false);

  // --- Données calculées ---

  /** Vrai si on est sur la première page */
  isFirstPage = computed(() => this.currentPage() === 0);

  /** Vrai si on est sur la dernière page */
  isLastPage = computed(() => this.currentPage() >= this.totalPages() - 1);

  /** Numéro de page affiché à l'utilisateur (commence à 1) */
  displayedPage = computed(() => this.currentPage() + 1);

  // --- Cycle de vie Angular ---

  ngOnInit(): void {
    this.loadAdmins();
  }

  // --- Chargement des données ---

  /**
   * Charge une page d'admins depuis l'API avec les filtres actifs.
   * Appelée à l'initialisation et après chaque changement de page ou de filtre.
   */
  loadAdmins(): void {
    this.isLoading.set(true);

    this.adminService
      .getAllAdmins(
        this.currentPage(),
        this.pageSize(),
        this.statusFilter(),
        this.roleFilter()
      )
      .subscribe({
        next: (response) => {
          // On stocke les admins de cette page et les métadonnées de pagination
          this.admins.set(response.content);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);
        },
        error: () => this.showMessage('Impossible de charger la liste des admins', 'error'),
        complete: () => this.isLoading.set(false),
      });
  }

  // --- Navigation entre les pages ---

  /**
   * Passe à la page suivante si elle existe.
   */
  nextPage(): void {
    if (!this.isLastPage()) {
      this.currentPage.update(p => p + 1);
      this.loadAdmins();
    }
  }

  /**
   * Revient à la page précédente si elle existe.
   */
  previousPage(): void {
    if (!this.isFirstPage()) {
      this.currentPage.update(p => p - 1);
      this.loadAdmins();
    }
  }

  // --- Changement de filtre ---

  /**
   * Applique un filtre par statut et recharge depuis la page 0.
   * @param status - La valeur du statut (ex: 'ACTIVE') ou undefined pour tous
   */
  applyStatusFilter(status: string | undefined): void {
    this.statusFilter.set(status);
    this.currentPage.set(0); // On repart toujours de la première page
    this.loadAdmins();
  }

  /**
   * Applique un filtre par rôle et recharge depuis la page 0.
   * @param role - La valeur du rôle (ex: 'REVIEWER') ou undefined pour tous
   */
  applyRoleFilter(role: string | undefined): void {
    this.roleFilter.set(role);
    this.currentPage.set(0);
    this.loadAdmins();
  }

  // --- Actions utilisateur ---

  /**
   * Active un admin identifié par son ID puis recharge la page courante.
   * @param adminId - L'identifiant unique de l'admin à activer
   */
  activateAdmin(adminId: string): void {
    this.isLoading.set(true);

    this.adminService.activateAdmin(adminId).subscribe({
      next: () => {
        this.showMessage('Admin activé avec succès', 'success');
        this.loadAdmins();
      },
      error: () => {
        this.showMessage("Erreur lors de l'activation", 'error');
        this.isLoading.set(false);
      },
    });
  }

  /**
   * Supprime un admin après confirmation puis recharge la page courante.
   * @param adminId - L'identifiant unique de l'admin à supprimer
   */
  deleteAdmin(adminId: string): void {
    const confirmed = confirm('Supprimer cet administrateur ? Cette action est irréversible.');
    if (!confirmed) return;

    this.isLoading.set(true);

    this.adminService.deleteAdmin(adminId).subscribe({
      next: () => {
        this.showMessage('Admin supprimé', 'success');
        this.loadAdmins();
      },
      error: () => {
        this.showMessage('Erreur lors de la suppression', 'error');
        this.isLoading.set(false);
      },
    });
  }

  // --- Utilitaires ---

  /**
   * Génère les 2 initiales d'un nom pour l'avatar textuel.
   * Exemple : "Jean Dupont" => "JD"
   * @param name - Le nom complet de l'admin
   */
  getInitials(name: string): string {
    return name
      .split(' ')
      .map(word => word[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  /**
   * Retourne le libellé lisible d'un statut admin.
   * @param status - La valeur de l'enum AdminStatus
   */
  getStatusLabel(status: AdminStatus): string {
    const labels: Record<string, string> = {
      ACTIVE:   'Actif',
      PENDING:  'En attente',
      INACTIVE: 'Inactif',
    };
    return labels[status] ?? status;
  }

  /**
   * Retourne la classe CSS du badge correspondant au statut.
   * @param status - La valeur de l'enum AdminStatus
   */
  getStatusClass(status: AdminStatus): string {
    const classes: Record<string, string> = {
      ACTIVE:   'badge badge-green',
      PENDING:  'badge badge-amber',
      INACTIVE: 'badge badge-gray',
    };
    return classes[status] ?? 'badge';
  }

  /**
   * Affiche un message toast pendant 3,5 secondes puis l'efface.
   * @param text - Le texte à afficher
   * @param type - 'success' pour vert, 'error' pour rouge
   */
  private showMessage(text: string, type: 'success' | 'error'): void {
    this.actionMessage.set({ text, type });
    setTimeout(() => this.actionMessage.set(null), 3500);
  }
}