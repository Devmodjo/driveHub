import { Routes } from '@angular/router';

/**
 * Une seule application, trois espaces :
 *
 *   /              vitrine     site public (landing, catalogue, connexion / inscription)   components/vitrine
 *   /dashboard     dashboard   espace des moniteurs et des élèves (données du tenant)     components/dashboard
 *   /backoffice    back-office administrateurs de la plateforme (schéma public)           components/back-office
 *
 * Chaque espace déclare ses routes dans son dossier et n'est chargé qu'à la première visite.
 */
export const routes: Routes = [
  {
    path: 'backoffice',
    loadChildren: () => import('./components/back-office/back-office.routes').then((m) => m.BACK_OFFICE_ROUTES),
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./components/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
  },
  // Anciennes adresses du back-office
  { path: 'login', redirectTo: 'backoffice/login' },
  { path: 'register', redirectTo: 'backoffice/register' },
  {
    path: '',
    loadChildren: () => import('./components/vitrine/vitrine.routes').then((m) => m.VITRINE_ROUTES),
  },
  { path: '**', redirectTo: '' },
];
