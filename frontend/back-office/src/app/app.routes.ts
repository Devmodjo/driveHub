import { Routes } from '@angular/router';
import { RegisterComponent } from './components/auth-component/register/register.component';
import { LoginComponent } from './components/auth-component/login/login.component';
import { DashboardComponent } from './components/dashboard-component/dashboard.component/dashboard.component';
import { authGuardGuard } from './guards/auth.guard';

export const routes: Routes = [
    {
        path: "register",
        component: RegisterComponent
    },
    {
        path: "login",
        component: LoginComponent
    },
    {
        path: "",
        redirectTo: "login",
        pathMatch: "full"
    },
    {
        path: "dashboard",
        // canActivate applique le guard — Angular appelle authGuard() avant d'afficher la page
        canActivate: [authGuardGuard],
        // loadComponent permet le chargement paresseux (lazy loading)
        loadComponent: () => import('./components/dashboard-component/dashboard.component/dashboard.component').then(m => m.DashboardComponent) 
    }
];
 