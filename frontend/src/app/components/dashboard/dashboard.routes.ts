import { Routes } from '@angular/router';
import { monitorGuard, sessionGuard, tenantGuard } from '../../guards/session.guard';

/**
 * Espace des moniteurs et des élèves (/dashboard).
 *  - /dashboard/bienvenue : tant que l'utilisateur n'a pas d'auto-école (pas de tenant dans le jeton) ;
 *  - les autres pages lisent le schéma de l'auto-école (tenantGuard) ;
 *  - élèves, demandes et véhicules sont réservés aux moniteurs (monitorGuard).
 */
export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    canActivate: [sessionGuard],
    loadComponent: () => import('./layout/dashboard-layout.component').then((m) => m.DashboardLayoutComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'accueil' },
      {
        path: 'bienvenue', title: 'Bienvenue - DriveHub',
        loadComponent: () => import('./pages/welcome/welcome.component').then((m) => m.WelcomeComponent),
      },
      {
        path: '',
        canActivateChild: [tenantGuard],
        children: [
          {
            path: 'accueil', title: 'Vue d\'ensemble - DriveHub',
            loadComponent: () => import('./pages/overview/overview.component').then((m) => m.OverviewComponent),
          },
          {
            path: 'reservations', title: 'Réservations - DriveHub',
            loadComponent: () => import('./pages/reservations/reservations.component').then((m) => m.ReservationsComponent),
          },
          {
            path: 'cours', title: 'Cours - DriveHub',
            loadComponent: () => import('./pages/courses/courses.component').then((m) => m.CoursesComponent),
          },
          {
            path: 'examens', title: 'Examens - DriveHub',
            loadComponent: () => import('./pages/exams/exams.component').then((m) => m.ExamsComponent),
          },
          {
            path: 'paiements', title: 'Paiements - DriveHub',
            loadComponent: () => import('./pages/payments/payments.component').then((m) => m.PaymentsComponent),
          },
          {
            path: 'eleves', title: 'Élèves - DriveHub', canActivate: [monitorGuard],
            loadComponent: () => import('./pages/students/students.component').then((m) => m.StudentsComponent),
          },
          {
            path: 'demandes', title: 'Demandes d\'adhésion - DriveHub', canActivate: [monitorGuard],
            loadComponent: () => import('./pages/join-requests/join-requests.component').then((m) => m.JoinRequestsComponent),
          },
          {
            path: 'vehicules', title: 'Véhicules - DriveHub', canActivate: [monitorGuard],
            loadComponent: () => import('./pages/vehicles/vehicles.component').then((m) => m.VehiclesComponent),
          },
        ],
      },
    ],
  },
];
