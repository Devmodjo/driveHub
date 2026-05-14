import { Routes } from '@angular/router';
import { RegisterComponent } from './components/auth-component/register/register.component';
import { LoginComponent } from './components/auth-component/login/login.component';
import { DashboardComponent } from './components/dashboard-component/dashboard.component/dashboard.component';

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
        component: DashboardComponent
    }
];
