import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AdminService } from '../../../services/admin-service/admin.service';
import { AuthService } from '../../../services/auth-service/auth.service';
import { AdminProfile } from '../../../interfaces/AdminProfile';
import UserRegisterModel from '../../../interfaces/UserRegisterModel';
import AdminStatus from '../../../enums/adminstatus.enum';
import { Role } from '../../../enums/role.enum';

@Component({
  selector: 'app-admins',
  imports: [DatePipe, FormsModule],
  providers: [AdminService],
  templateUrl: './admins.component.html',
  styleUrl: './admins.component.css',
})
export class AdminsComponent implements OnInit {

  private adminService = inject(AdminService);

  private authService = inject(AuthService);

  admins = signal<AdminProfile[]>([]);

  currentPage = signal<number>(0);

  pageSize = signal<number>(10);

  totalPages = signal<number>(0);

  totalElements = signal<number>(0);

  statusFilter = signal<string | undefined>(undefined);

  roleFilter = signal<string | undefined>(undefined);

  actionMessage = signal<{ text: string; type: 'success' | 'error' } | null>(null);

  isLoading = signal<boolean>(false);

  currentUserRole = signal<Role | null>(null);

  selectedAdmin = signal<AdminProfile | null>(null);

  showDetailDrawer = signal<boolean>(false);

  showCreateDrawer = signal<boolean>(false);

  createFormData = signal<UserRegisterModel>({
    name: '',
    email: '',
    password: '',
    role: Role.REVIEWER,
    residence: '',
    phoneNumber: '',
    reason: '',
  });

  isSubmitting = signal<boolean>(false);

  isRoot = computed(() => this.currentUserRole() === Role.ROOT);
  isSuperAdmin = computed(() => this.currentUserRole() === Role.SUPER_ADMIN);

  canActivate = computed(() => this.isRoot() || this.isSuperAdmin());

  isFirstPage = computed(() => this.currentPage() === 0);
  isLastPage = computed(() => this.currentPage() >= this.totalPages() - 1);

  displayedPage = computed(() => this.currentPage() + 1);


  /**
   * Initialisation du composant.
   * Charge le role de l'utilisateur connecte puis recupere la premiere page d'administrateurs.
   */
  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadAdmins();
  }


  /**
   * Recupere le profil de l'utilisateur connecte via AuthService
   * et extrait son role pour piloter les regles RBAC du template.
   */
  loadCurrentUser(): void {
    this.authService.getCurrentAdmin().subscribe({
      next: (profile) => this.currentUserRole.set(profile.role),
      error: () => this.showMessage('Impossible de charger le profil utilisateur', 'error'),
    });
  }

  /**
   * Charge la liste paginee des administrateurs en fonction
   * de la page courante, de la taille de page et des filtres actifs.
   *
   * Met a jour les signaux `admins`, `totalPages` et `totalElements`
   * a partir de la reponse paginee de l'API.
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
          this.admins.set(response.content);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);
        },
        error: () => this.showMessage('Impossible de charger la liste des admins', 'error'),
        complete: () => this.isLoading.set(false),
      });
  }


  /** Passe a la page suivante si la page courante n'est pas la derniere. */
  nextPage(): void {
    if (!this.isLastPage()) {
      this.currentPage.update(p => p + 1);
      this.loadAdmins();
    }
  }

  /** Revient a la page precedente si la page courante n'est pas la premiere. */
  previousPage(): void {
    if (!this.isFirstPage()) {
      this.currentPage.update(p => p - 1);
      this.loadAdmins();
    }
  }


  /**
   * Applique un filtre par statut et recharge la liste depuis la premiere page.
   * @param status - Valeur du statut a filtrer, ou `undefined` pour retirer le filtre.
   */
  applyStatusFilter(status: string | undefined): void {
    this.statusFilter.set(status);
    this.currentPage.set(0);
    this.loadAdmins();
  }

  /**
   * Applique un filtre par role et recharge la liste depuis la premiere page.
   * @param role - Valeur du role a filtrer, ou `undefined` pour retirer le filtre.
   */
  applyRoleFilter(role: string | undefined): void {
    this.roleFilter.set(role);
    this.currentPage.set(0);
    this.loadAdmins();
  }


  /**
   * Ouvre le drawer de detail pour l'administrateur selectionne.
   * Ferme le drawer de creation s'il etait ouvert (un seul drawer a la fois).
   */
  openDetail(admin: AdminProfile): void {
    this.selectedAdmin.set(admin);
    this.showCreateDrawer.set(false);
    this.showDetailDrawer.set(true);
  }

  /**
   * Ouvre le drawer de creation d'un nouvel administrateur.
   * Reinitialise le formulaire et ferme le drawer de detail s'il etait ouvert.
   */
  openCreateDrawer(): void {
    this.showDetailDrawer.set(false);
    this.resetCreateForm();
    this.showCreateDrawer.set(true);
  }

  /**
   * Ferme tous les drawers et reinitialise l'administrateur selectionne.
   */
  closeDrawers(): void {
    this.showDetailDrawer.set(false);
    this.showCreateDrawer.set(false);
    this.selectedAdmin.set(null);
  }


  /**
   * Cree un nouvel administrateur via l'API.
   *
   * Validation locale minimale : les champs `name`, `email` et `password` sont obligatoires.
   * En cas de succes, le drawer est ferme et la liste est rechargee.
   * Le signal `isSubmitting` empeche les doubles soumissions pendant l'appel.
   */
  createAdmin(): void {
    const formData = this.createFormData();
    if (!formData.name || !formData.email || !formData.password) {
      this.showMessage('Veuillez remplir tous les champs obligatoires', 'error');
      return;
    }

    this.isSubmitting.set(true);

    this.adminService.createAdmin(formData).subscribe({
      next: () => {
        this.showMessage('Administrateur cree avec succes', 'success');
        this.closeDrawers();
        this.loadAdmins();
      },
      error: () => {
        this.showMessage('Erreur lors de la creation de l\'administrateur', 'error');
        this.isSubmitting.set(false);
      },
      complete: () => this.isSubmitting.set(false),
    });
  }

  /**
   * Active un administrateur en attente (statut PENDING -> ACTIVE).
   * Accessible uniquement aux utilisateurs ROOT et SUPER_ADMIN (RBAC).
   *
   * @param adminId - Identifiant unique de l'administrateur a activer.
   */
  activateAdmin(adminId: string): void {
    this.isLoading.set(true);

    this.adminService.activateAdmin(adminId).subscribe({
      next: () => {
        this.showMessage('Admin active avec succes', 'success');
        this.closeDrawers();
        this.loadAdmins();
      },
      error: () => {
        this.showMessage('Erreur lors de l\'activation', 'error');
        this.isLoading.set(false);
      },
    });
  }

  /**
   * Supprime un administrateur apres confirmation de l'utilisateur.
   * Reserve exclusivement au role ROOT.
   *
   * @param adminId - Identifiant unique de l'administrateur a supprimer.
   */
  deleteAdmin(adminId: string): void {
    const confirmed = confirm('Supprimer cet administrateur ? Cette action est irreversible.');
    if (!confirmed) return;

    this.isLoading.set(true);

    this.adminService.deleteAdmin(adminId).subscribe({
      next: () => {
        this.showMessage('Admin supprime', 'success');
        this.closeDrawers();
        this.loadAdmins();
      },
      error: () => {
        this.showMessage('Erreur lors de la suppression', 'error');
        this.isLoading.set(false);
      },
    });
  }


  /**
   * Met a jour un champ specifique du formulaire de creation de maniere immutable.
   * Utilise une copie superficielle de l'objet pour respecter la reactivity des signaux.
   *
   * @param field - Cle du champ a modifier dans `UserRegisterModel`.
   * @param value - Nouvelle valeur du champ.
   */
  updateCreateField(field: keyof UserRegisterModel, value: string): void {
    this.createFormData.update(current => ({ ...current, [field]: value }));
  }


  /**
   * Extrait les initiales d'un nom complet pour l'affichage dans l'avatar.
   * Retourne au maximum deux caracteres en majuscules.
   *
   * @param name - Nom complet de l'administrateur (ex. "Jean Dupont").
   * @returns Les initiales (ex. "JD").
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
   * Retourne le libelle francais correspondant a un statut d'administrateur.
   * Sert a la traduction des valeurs brutes de l'enum vers un texte lisible.
   *
   * @param status - Valeur de l'enum `AdminStatus`.
   * @returns Le libelle traduit, ou la valeur brute si aucune correspondance.
   */
  getStatusLabel(status: AdminStatus): string {
    const labels: Record<string, string> = {
      ACTIVE: 'Actif',
      PENDING: 'En attente',
      INACTIVE: 'Inactif',
      EMAIL_PENDING: 'Email en attente',
    };
    return labels[status] ?? status;
  }

  /**
   * Retourne la classe CSS correspondant a un statut d'administrateur.
   * Permet d'appliquer un badge colore dans le template (vert, ambre, gris, bleu).
   *
   * @param status - Valeur de l'enum `AdminStatus`.
   * @returns La chaine de classes CSS a appliquer au badge.
   */
  getStatusClass(status: AdminStatus): string {
    const classes: Record<string, string> = {
      ACTIVE: 'badge badge-green',
      PENDING: 'badge badge-amber',
      INACTIVE: 'badge badge-gray',
      EMAIL_PENDING: 'badge badge-blue',
    };
    return classes[status] ?? 'badge';
  }

  /**
   * Retourne le libelle lisible correspondant a un role d'administrateur.
   *
   * @param role - Valeur de l'enum `Role`.
   * @returns Le libelle traduit, ou la valeur brute si aucune correspondance.
   */
  getRoleLabel(role: Role): string {
    const labels: Record<string, string> = {
      ROOT: 'Root',
      SUPER_ADMIN: 'Super Admin',
      REVIEWER: 'Reviewer',
    };
    return labels[role] ?? role;
  }

  /**
   * Reinitialise le formulaire de creation a ses valeurs par defaut.
   * Appelee automatiquement a l'ouverture du drawer de creation.
   */
  private resetCreateForm(): void {
    this.createFormData.set({
      name: '',
      email: '',
      password: '',
      role: Role.REVIEWER,
      residence: '',
      phoneNumber: '',
      reason:''
    });
  }

  /**
   * Affiche un message toast temporaire pendant 3,5 secondes.
   * Utilise pour notifier l'utilisateur du resultat d'une action (succes ou erreur).
   *
   * @param text - Contenu du message a afficher.
   * @param type - Type du message : 'success' pour une confirmation, 'error' pour un echec.
   */
  private showMessage(text: string, type: 'success' | 'error'): void {
    this.actionMessage.set({ text, type });
    setTimeout(() => this.actionMessage.set(null), 3500);
  }
}