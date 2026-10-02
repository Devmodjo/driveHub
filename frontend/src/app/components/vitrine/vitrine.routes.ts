import { Routes } from '@angular/router';
import { VitrineLayoutComponent } from './layout/vitrine-layout.component';
import { LandingComponent } from './pages/landing/landing.component';

/** Site public : accueil, catalogue des auto-écoles, connexion / inscription des moniteurs et élèves. */
export const VITRINE_ROUTES: Routes = [
  {
    path: '',
    component: VitrineLayoutComponent,
    children: [
      { path: '', component: LandingComponent, title: 'DriveHub - Gestion d\'auto-école' },
      {
        path: 'auto-ecoles', title: 'Trouver une auto-école - DriveHub',
        loadComponent: () => import('./pages/schools/schools.component').then((m) => m.SchoolsComponent),
      },
      {
        path: 'connexion', title: 'Connexion - DriveHub',
        loadComponent: () => import('./pages/auth/login.component').then((m) => m.UserLoginComponent),
      },
      {
        path: 'inscription', title: 'Inscription - DriveHub',
        loadComponent: () => import('./pages/auth/register.component').then((m) => m.UserRegisterComponent),
      },
      {
        path: 'mot-de-passe-oublie', title: 'Mot de passe oublié - DriveHub',
        loadComponent: () => import('./pages/auth/forgot-password.component').then((m) => m.ForgotPasswordComponent),
      },
      // Liens envoyés par email par le backend (AuthServiceImpl) : ne pas renommer ces chemins
      {
        path: 'verify-email', title: 'Vérification de l\'email - DriveHub',
        loadComponent: () => import('./pages/auth/verify-email.component').then((m) => m.VerifyEmailComponent),
      },
      {
        path: 'reset-password', title: 'Nouveau mot de passe - DriveHub',
        loadComponent: () => import('./pages/auth/reset-password.component').then((m) => m.ResetPasswordComponent),
      },
    ],
  },
];
