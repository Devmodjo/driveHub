import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { LucideDynamicIcon, LucideIcon } from '@lucide/angular';
import { AuthService } from '../../../../services/auth-service/auth.service';
import { AdminProfile } from '../../../../interfaces/AdminProfile';
import { ThemeService } from '../../../../services/theme-service/theme.service';
import { ICONS } from '../../../../shared/icons';
import { initials, roleLabel } from '../../shared/bo-status';
import { ProfilePanelComponent } from '../profile-panel/profile-panel.component';

/** Une entrée du menu latéral. */
interface NavLink {
  label: string;
  route: string;
  icon: LucideIcon;
  /** Si vrai, l'entrée n'est affichée qu'aux administrateurs ROOT. */
  rootOnly?: boolean;
}

/**
 * Navigation du back-office.
 *
 * - Mobile (moins de 1024px) : barre en haut avec un bouton « menu » qui ouvre la barre latérale
 *   en tiroir (off-canvas) par-dessus le contenu, avec un voile sombre pour la refermer.
 * - À partir de lg: : barre latérale fixe à gauche, toujours visible.
 *
 * Elle affiche aussi l'administrateur connecté (GET /admin/me), le bouton de thème (ThemeService),
 * l'accès au volet « Mon profil » et la déconnexion (AuthService.logout()).
 */
@Component({
  selector: 'app-dashboard-navigation',
  imports: [RouterLink, RouterLinkActive, LucideDynamicIcon, ProfilePanelComponent],
  host: { '(document:keydown.escape)': 'isMenuOpen.set(false)' },
  templateUrl: './dashboard.navigation.component.html',
})
export class DashboardNavigationComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private theme = inject(ThemeService);
  protected readonly icons = ICONS;
  protected readonly roleLabel = roleLabel;

  /** Profil de l'admin connecté (null tant qu'il n'est pas chargé). */
  adminProfile = signal<AdminProfile | null>(null);
  userInitials = computed(() => initials(this.adminProfile()?.name));
  get isDark(): boolean { return this.theme.isDark(); }

  /** Tiroir de navigation ouvert (mobile uniquement). */
  isMenuOpen = signal(false);
  /** Volet « Mon profil » ouvert. */
  isProfileOpen = signal(false);

  // ─── Liens de navigation ───────────────────────────────────────────
  // Pour ajouter un lien : ajouter un objet ici + la route dans back-office.routes.ts
  private readonly allLinks: NavLink[] = [
    { label: 'Vue d\'ensemble', route: '/backoffice/dashboard/overview', icon: ICONS.LayoutDashboard },
    { label: 'Auto-écoles', route: '/backoffice/dashboard/schools', icon: ICONS.School },
    { label: 'En attente', route: '/backoffice/dashboard/pending', icon: ICONS.Clock },
    { label: 'Admins', route: '/backoffice/dashboard/admins', icon: ICONS.Users, rootOnly: true },
    { label: 'Emails', route: '/backoffice/dashboard/emails', icon: ICONS.Mail },
  ];

  /**
   * Liens visibles : la liste des admins est réservée aux ROOT (l'API répond 403 sinon).
   * Tant que le profil n'est pas chargé, on affiche tout plutôt que de cacher un lien à tort.
   */
  navLinks = computed(() => {
    const role = this.adminProfile()?.role;
    return this.allLinks.filter((link) => !link.rootOnly || !role || role === 'ROOT');
  });

  ngOnInit(): void {
    this.authService.getCurrentAdmin().subscribe({
      next: (profile) => this.adminProfile.set(profile),
      error: () => this.adminProfile.set(null),
    });
  }

  toggleMenu(): void {
    this.isMenuOpen.update((open) => !open);
  }

  toggleTheme(): void {
    this.theme.toggle();
  }

  openProfile(): void {
    this.isMenuOpen.set(false);
    this.isProfileOpen.set(true);
  }

  /** Déconnecte l'administrateur (le backend invalide le jeton) puis retour à la connexion. */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/backoffice/login']);
  }
}
