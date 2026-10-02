import { Routes } from '@angular/router';
import { authGuardGuard } from '../../guards/auth.guard';
import { LoginComponent } from './auth-component/login/login.component';
import { RegisterComponent } from './auth-component/register/register.component';

/** Back-office des administrateurs de la plateforme DriveHub (/backoffice). */
export const BACK_OFFICE_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'register', component: RegisterComponent, title: 'Back-office - Inscription' },
  { path: 'login', component: LoginComponent, title: 'Back-office - Connexion' },
  // Lien envoyé par email (PlatformAdminService) : /backoffice/verify-email?token=...
  {
    path: 'verify-email', data: { admin: true },
    loadComponent: () => import('../vitrine/pages/auth/verify-email.component').then((m) => m.VerifyEmailComponent),
  },
  {
    path: 'dashboard',
    canActivate: [authGuardGuard],
    // Chargement paresseux du composant principal (coque)
    loadComponent: () => import('./dashboard-component/dashboard.component/dashboard.component')
      .then((m) => m.DashboardComponent),
    // Routes enfants : chaque entrée de la sidebar a sa propre route
    children: [
      { path: '', redirectTo: 'overview', pathMatch: 'full' },
      {
        path: 'overview',
        loadComponent: () => import('./pages/overview/overview.component').then((m) => m.OverviewComponent),
      },
      {
        path: 'admins',
        loadComponent: () => import('./pages/admins/admins.component').then((m) => m.AdminsComponent),
      },
      {
        path: 'schools',
        loadComponent: () => import('./pages/autoschool/school.component').then((m) => m.SchoolComponent),
      },
      {
        path: 'pending',
        loadComponent: () => import('./pages/pending/pending.component').then((m) => m.PendingComponent),
      },
    ],
  },
];
