import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LucideDynamicIcon, LucideIcon } from '@lucide/angular';
import { SessionService } from '../../../services/session-service/session.service';
import { ThemeService } from '../../../services/theme-service/theme.service';
import { ICONS } from '../../../shared/icons';

interface NavItem {
  label: string;
  path: string;
  icon: LucideIcon;
}

/**
 * Coque du dashboard des moniteurs et des élèves : barre latérale + contenu.
 * Les entrées de menu dépendent du rôle et de l'appartenance à une auto-école (tenant du jeton).
 */
@Component({
  selector: 'app-dashboard-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LucideDynamicIcon],
  template: `
    <div class="min-h-screen bg-[#fafafa] dark:bg-[#080808] text-black dark:text-white font-sans">
      <!-- Barre du haut (mobile) -->
      <header class="lg:hidden sticky top-0 z-40 flex items-center justify-between px-5 py-3 bg-white/90 dark:bg-black/90 backdrop-blur border-b border-black/5 dark:border-white/5">
        <a routerLink="/" class="font-black italic tracking-tighter text-xl"><span class="text-[#0070f3]">Drive</span>Hub</a>
        <button (click)="menuOpen.set(!menuOpen())" aria-label="Menu"><svg [lucideIcon]="menuOpen() ? icons.X : icons.Menu" [size]="26" /></button>
      </header>

      <div class="flex">
        <aside class="fixed lg:sticky top-0 z-30 h-screen w-72 shrink-0 flex-col bg-white dark:bg-black border-r border-black/5 dark:border-white/5 p-6 transition-transform lg:translate-x-0 lg:flex"
               [class]="menuOpen() ? 'flex translate-x-0' : 'hidden -translate-x-full'">
          <a routerLink="/" class="flex items-center gap-3 mb-10">
            <div class="relative w-10 h-10 rounded-xl overflow-hidden bg-black border border-black/5 dark:border-white/10">
              <img src="/dh_icon.png" alt="DriveHub" class="absolute inset-0 w-full h-full object-cover" />
            </div>
            <span class="text-xl font-black italic tracking-tighter"><span class="text-[#0070f3]">Drive</span>Hub</span>
          </a>

          <nav class="flex flex-col gap-1 grow overflow-y-auto">
            @for (item of navItems(); track item.path) {
              <a [routerLink]="item.path" routerLinkActive="bg-[#0070f3]/10 text-[#0070f3]" [routerLinkActiveOptions]="{ exact: true }"
                 (click)="menuOpen.set(false)"
                 class="flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold text-black/70 dark:text-white/70 hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                <svg [lucideIcon]="item.icon" [size]="18" /> {{ item.label }}
              </a>
            }
          </nav>

          <div class="pt-6 border-t border-black/5 dark:border-white/5 space-y-3">
            <div class="text-sm">
              <p class="font-bold truncate">{{ session.claims()?.email }}</p>
              <p class="text-black/50 dark:text-white/50">{{ session.isMonitor() ? 'Moniteur' : 'Élève' }}</p>
            </div>
            <div class="flex gap-2">
              <button class="btn-small bg-black/5 dark:bg-white/5" (click)="theme.toggle()" aria-label="Changer de thème">
                <svg [lucideIcon]="theme.isDark() ? icons.Sun : icons.Moon" [size]="16" />
              </button>
              <button class="btn-small bg-black/5 dark:bg-white/5 grow" (click)="logout()">
                <svg [lucideIcon]="icons.LogOut" [size]="16" /> Déconnexion
              </button>
            </div>
          </div>
        </aside>

        <main class="grow min-w-0 p-5 md:p-10">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
})
export class DashboardLayoutComponent {
  protected readonly icons = ICONS;
  protected readonly session = inject(SessionService);
  protected readonly theme = inject(ThemeService);
  private readonly router = inject(Router);
  protected readonly menuOpen = signal(false);

  protected readonly navItems = computed<NavItem[]>(() => {
    if (!this.session.tenant()) {
      return [{ label: 'Bienvenue', path: '/dashboard/bienvenue', icon: ICONS.LayoutDashboard }];
    }
    const common: NavItem[] = [
      { label: 'Vue d\'ensemble', path: '/dashboard/accueil', icon: ICONS.LayoutDashboard },
      { label: 'Réservations', path: '/dashboard/reservations', icon: ICONS.Calendar },
      { label: 'Cours', path: '/dashboard/cours', icon: ICONS.BookOpen },
      { label: 'Examens', path: '/dashboard/examens', icon: ICONS.GraduationCap },
      { label: 'Paiements', path: '/dashboard/paiements', icon: ICONS.Wallet },
    ];
    if (!this.session.isMonitor()) {
      return common;
    }
    return [
      ...common.slice(0, 1),
      { label: 'Élèves', path: '/dashboard/eleves', icon: ICONS.Users },
      { label: 'Demandes d\'adhésion', path: '/dashboard/demandes', icon: ICONS.UserPlus },
      { label: 'Véhicules', path: '/dashboard/vehicules', icon: ICONS.Car },
      ...common.slice(1),
    ];
  });

  protected logout(): void {
    this.session.logout();
    this.router.navigate(['/connexion']);
  }
}
