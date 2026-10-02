import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../../services/auth-service/auth.service';
import { AdminProfile } from '../../../../interfaces/AdminProfile';
import { ThemeService } from '../../../../services/theme-service/theme.service';

/**
 * Composant Sidebar — barre de navigation latérale du dashboard.
 *
 * Responsabilités :
 * - Afficher les liens de navigation avec l'état actif (routerLinkActive)
 * - Afficher le nom et le rôle de l'admin connecté (via AuthService.getCurrentAdmin())
 * - Gérer le toggle Light / Dark mode (ThemeService, partagé avec la vitrine et le dashboard)
 */
@Component({
  selector: 'app-dashboard-navigation',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './dashboard.navigation.component.html',
  styleUrl: './dashboard.navigation.component.css',
})
export class DashboardNavigationComponent implements OnInit {

  private authService = inject(AuthService);
  private router = inject(Router);

  // Profil de l'admin connecté (null tant que non chargé)
  adminProfile: AdminProfile | null = null;
  // Les initiales, pré-calculées pour éviter NG0100 (ExpressionChangedAfterItHasBeenCheckedError)
  userInitials: string = '?';

  // Thème partagé par toute l'application (classe "dark" sur <html>)
  private theme = inject(ThemeService);
  get isDark(): boolean { return this.theme.isDark(); }

  // ─── Liens de navigation ─────────────────────────────────────────────────────
  // Pour ajouter un lien : ajouter un objet ici + créer la route dans app.routes.ts
  navLinks = [
    { label: 'Vue d\'ensemble', route: '/backoffice/dashboard/overview' },
    { label: 'Admins',          route: '/backoffice/dashboard/admins'   },
    { label: 'En attente',      route: '/backoffice/dashboard/pending'  },
  ];

  // Contrôle du menu mobile (hamburger)
  isMenuOpen = false;
  toggleMenu() { this.isMenuOpen = !this.isMenuOpen; }

  ngOnInit(): void {
    // Charger le profil de l'admin connecté dès que la sidebar s'affiche
    this.authService.getCurrentAdmin().subscribe({
      next: (profile) => { 
        this.adminProfile = profile; 
        this.userInitials = this.computeInitials(profile.name);
      },
      error: () => { 
        this.adminProfile = null;    
        this.userInitials = '?';
      }
    });
  }

  /**
   * Bascule entre le mode light et dark.
   */
  toggleTheme(): void {
    this.theme.toggle();
  }

  /**
   * Déconnecte l'utilisateur et le redirige vers /login.
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/backoffice/login']);
  }

  /**
   * Retourne les initiales calculées à partir du nom
   */
  private computeInitials(name: string | undefined): string {
    if (!name) return '?';
    return name
      .split(' ')
      .map(w => w[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }
}

