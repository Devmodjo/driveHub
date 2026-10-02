import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DatePipe, NgTemplateOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { LucideDynamicIcon } from '@lucide/angular';

import { AdminService } from '../../../../services/admin-service/admin.service';
import { AuthService } from '../../../../services/auth-service/auth.service';
import { AdminProfile } from '../../../../interfaces/AdminProfile';
import { ApiResponse } from '../../../../interfaces/ApiResponse';
import UserRegisterModel from '../../../../interfaces/UserRegisterModel';
import { Role } from '../../../../enums/role.enum';
import { errorMessage, fieldErrors } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { BoConfirmComponent } from '../../shared/bo-confirm.component';
import { BoDrawerComponent } from '../../shared/bo-drawer.component';
import { BoToastComponent, BoToastMessage } from '../../shared/bo-toast.component';
import { adminStatusClass, adminStatusLabel, initials, roleLabel } from '../../shared/bo-status';

/** Action sensible qui attend la confirmation (désactivation ou suppression). */
interface PendingAction {
  kind: 'deactivate' | 'delete';
  admin: AdminProfile;
}

/**
 * Page « Administrateurs » (/backoffice/dashboard/admins), réservée aux ROOT côté API.
 * Liste paginée et filtrable, détail, création, activation / désactivation et suppression.
 *
 * Mobile : une carte par administrateur ; à partir de md: : tableau.
 */
@Component({
  selector: 'app-admins',
  imports: [DatePipe, NgTemplateOutlet, FormsModule, LucideDynamicIcon, BoConfirmComponent, BoDrawerComponent, BoToastComponent],
  templateUrl: './admins.component.html',
})
export class AdminsComponent implements OnInit {
  private adminService = inject(AdminService);
  private authService = inject(AuthService);
  protected readonly icons = ICONS;
  protected readonly statusLabel = adminStatusLabel;
  protected readonly statusClass = adminStatusClass;
  protected readonly roleLabel = roleLabel;
  protected readonly initials = initials;

  admins = signal<AdminProfile[]>([]);
  currentPage = signal<number>(0);
  pageSize = signal<number>(10);
  totalPages = signal<number>(0);
  totalElements = signal<number>(0);
  statusFilter = signal<string | undefined>(undefined);
  roleFilter = signal<string | undefined>(undefined);
  actionMessage = signal<BoToastMessage | null>(null);
  isLoading = signal<boolean>(false);
  /** Message affiché à la place de la liste si elle ne peut pas être chargée (ex. 403). */
  listError = signal<string>('');
  currentUserRole = signal<Role | null>(null);

  selectedAdmin = signal<AdminProfile | null>(null);
  showDetailDrawer = signal<boolean>(false);
  showCreateDrawer = signal<boolean>(false);
  pendingAction = signal<PendingAction | null>(null);
  /** Identifiant de l'admin en cours de traitement. */
  busyId = signal<string | null>(null);

  createFormData = signal<UserRegisterModel>(this.emptyForm());
  isSubmitting = signal<boolean>(false);
  createError = signal<string>('');
  createFieldErrors = signal<Record<string, string>>({});

  isRoot = computed(() => this.currentUserRole() === Role.ROOT);
  isSuperAdmin = computed(() => this.currentUserRole() === Role.SUPER_ADMIN);
  canActivate = computed(() => this.isRoot() || this.isSuperAdmin());

  isFirstPage = computed(() => this.currentPage() === 0);
  isLastPage = computed(() => this.currentPage() >= this.totalPages() - 1);
  displayedPage = computed(() => this.currentPage() + 1);

  /** Onglets de filtre par statut (undefined = tous). */
  protected readonly statusTabs: { label: string; value: string | undefined }[] = [
    { label: 'Tous', value: undefined },
    { label: 'Actifs', value: 'ACTIVE' },
    { label: 'En attente', value: 'PENDING' },
    { label: 'Suspendus', value: 'SUSPENDED' },
    { label: 'Inactifs', value: 'INACTIVE' },
    { label: 'Email non vérifié', value: 'EMAIL_PENDING' },
  ];

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadAdmins();
  }

  /** Récupère le rôle de l'utilisateur connecté, qui pilote les boutons visibles (RBAC). */
  loadCurrentUser(): void {
    this.authService.getCurrentAdmin().subscribe({
      next: (profile) => this.currentUserRole.set(profile.role),
      error: (err) => this.showMessage(errorMessage(err, 'Impossible de charger votre profil.'), 'error'),
    });
  }

  /** Charge la page courante des administrateurs selon les filtres actifs. */
  loadAdmins(): void {
    this.isLoading.set(true);
    this.listError.set('');
    this.adminService
      .getAllAdmins(this.currentPage(), this.pageSize(), this.statusFilter(), this.roleFilter())
      .subscribe({
        next: (response) => {
          this.admins.set(response.content);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.admins.set([]);
          this.listError.set(err?.status === 403
            ? 'La gestion des administrateurs est réservée aux comptes ROOT.'
            : errorMessage(err, 'Impossible de charger la liste des administrateurs.'));
        },
      });
  }

  nextPage(): void {
    if (!this.isLastPage()) {
      this.currentPage.update((p) => p + 1);
      this.loadAdmins();
    }
  }

  previousPage(): void {
    if (!this.isFirstPage()) {
      this.currentPage.update((p) => p - 1);
      this.loadAdmins();
    }
  }

  /** Filtre par statut et revient à la première page. */
  applyStatusFilter(status: string | undefined): void {
    this.statusFilter.set(status);
    this.currentPage.set(0);
    this.loadAdmins();
  }

  /** Filtre par rôle et revient à la première page. */
  applyRoleFilter(role: string | undefined): void {
    this.roleFilter.set(role);
    this.currentPage.set(0);
    this.loadAdmins();
  }

  /** Ouvre le volet de détail (un seul volet ouvert à la fois). */
  openDetail(admin: AdminProfile): void {
    this.selectedAdmin.set(admin);
    this.showCreateDrawer.set(false);
    this.showDetailDrawer.set(true);
  }

  /** Ouvre le volet de création avec un formulaire vide. */
  openCreateDrawer(): void {
    this.showDetailDrawer.set(false);
    this.createFormData.set(this.emptyForm());
    this.createError.set('');
    this.createFieldErrors.set({});
    this.showCreateDrawer.set(true);
  }

  closeDrawers(): void {
    this.showDetailDrawer.set(false);
    this.showCreateDrawer.set(false);
    this.selectedAdmin.set(null);
  }

  /** Crée un administrateur ; les erreurs du backend s'affichent sous les champs concernés. */
  createAdmin(): void {
    const formData = this.createFormData();
    if (!formData.name.trim() || !formData.email.trim() || !formData.password) {
      this.createError.set('Le nom, l\'email et le mot de passe sont obligatoires.');
      return;
    }

    this.isSubmitting.set(true);
    this.createError.set('');
    this.createFieldErrors.set({});

    this.adminService.createAdmin({ ...formData, email: formData.email.trim() }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.showMessage('Administrateur créé.', 'success');
        this.closeDrawers();
        this.loadAdmins();
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.createFieldErrors.set(fieldErrors(err));
        this.createError.set(errorMessage(err, 'Erreur lors de la création de l\'administrateur.'));
      },
    });
  }

  /** Active un compte en attente ou réactive un compte suspendu (ROOT côté API). */
  activateAdmin(adminId: string): void {
    this.run(adminId, this.adminService.activateAdmin(adminId), 'Administrateur activé.', 'Erreur lors de l\'activation.');
  }

  /** Demande confirmation avant de désactiver un administrateur. */
  askDeactivate(admin: AdminProfile): void {
    this.pendingAction.set({ kind: 'deactivate', admin });
  }

  /** Demande confirmation avant de supprimer un administrateur. */
  deleteAdmin(admin: AdminProfile): void {
    this.pendingAction.set({ kind: 'delete', admin });
  }

  confirmPendingAction(): void {
    const action = this.pendingAction();
    if (!action) return;
    const id = action.admin.id;
    if (action.kind === 'deactivate') {
      this.run(id, this.adminService.disableAdmin(id), 'Administrateur désactivé.', 'Erreur lors de la désactivation.');
    } else {
      this.run(id, this.adminService.deleteAdmin(id), 'Administrateur supprimé.', 'Erreur lors de la suppression.');
    }
  }

  /** Met à jour un champ du formulaire de création (copie de l'objet pour que le signal change). */
  updateCreateField(field: keyof UserRegisterModel, value: string): void {
    this.createFormData.update((current) => ({ ...current, [field]: value }));
  }

  /** Initiales pour l'avatar (ex. « Jean Dupont » → « JD »). */
  getInitials(name: string): string {
    return initials(name);
  }

  getStatusLabel(status: string): string {
    return adminStatusLabel(status);
  }

  getRoleLabel(role: Role): string {
    return roleLabel(role);
  }

  /** Exécute une action de l'API sur un admin puis recharge la liste. */
  private run(id: string, call: Observable<ApiResponse>, success: string, failure: string): void {
    this.busyId.set(id);
    call.subscribe({
      next: () => {
        this.busyId.set(null);
        this.pendingAction.set(null);
        this.closeDrawers();
        this.showMessage(success, 'success');
        this.loadAdmins();
      },
      error: (err) => {
        this.busyId.set(null);
        this.pendingAction.set(null);
        this.showMessage(errorMessage(err, failure), 'error');
      },
    });
  }

  private emptyForm(): UserRegisterModel {
    return { name: '', email: '', password: '', role: Role.REVIEWER, residence: '', phoneNumber: '', reason: '' };
  }

  /** Affiche un message temporaire pendant 3,5 secondes. */
  private showMessage(text: string, type: 'success' | 'error'): void {
    this.actionMessage.set({ text, type });
    setTimeout(() => this.actionMessage.set(null), 3500);
  }
}
