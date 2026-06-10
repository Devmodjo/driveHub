import { Routes } from '@angular/router';
import { RegisterComponent } from './components/auth-component/register/register.component';
import { LoginComponent } from './components/auth-component/login/login.component';
import { authGuardGuard } from './guards/auth.guard';

export const routes: Routes = [
    { path: 'register', component: RegisterComponent },
    { path: 'login',    component: LoginComponent    },
    { path: '', redirectTo: 'login', pathMatch: 'full' },
    {
        path: 'dashboard',
        canActivate: [authGuardGuard],
        // Chargement paresseux du composant principal (coque)
        loadComponent: () => import('./components/dashboard-component/dashboard.component/dashboard.component')
            .then(m => m.DashboardComponent),
        // Routes enfants : chaque entrée de la sidebar a sa propre route
        children: [
            // Redirection par défaut vers /dashboard/overview
            { path: '', redirectTo: 'overview', pathMatch: 'full' },
            {
                path: 'overview',
                loadComponent: () => import('./pages/dashboard/overview/overview.component')
                    .then(m => m.OverviewComponent)
            },
            {
                path: 'admins',
                loadComponent: () => import('./pages/dashboard/admins/admins.component')
                    .then(m => m.AdminsComponent)
            },
            {
                path: 'schools',
                loadComponent : () =>  import('./pages/dashboard/autoschool/school.component')
                    .then(m => m.SchoolComponent)
            },
            {
                path: 'pending',
                loadComponent: () => import('./pages/dashboard/pending/pending.component')
                    .then(m => m.PendingComponent)
            }
        ]
    }
];