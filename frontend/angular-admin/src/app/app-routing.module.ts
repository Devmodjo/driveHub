import { NgModule } from '@angular/core';
import { provideRouter, RouterModule, Routes, withHashLocation } from '@angular/router';
import { GuestComponent } from './theme/layout/guest/guest.component';
import { AdminComponent } from './theme/layout/admin/admin.component';
import { AuthGuard, LoginGuard } from './utils';
import { P404Component } from './views/error/404.component';
import { HashLocationStrategy, LocationStrategy } from '@angular/common';
import { AdminGuard } from './utils/admin.guard';

const routes: Routes = [
  {
    path: '',
    component: GuestComponent,
    children: [
      {
        path: '',
        redirectTo: '/login',
        pathMatch: 'full'
      },
      {
        path: 'login',
        canActivate: [LoginGuard],
        loadComponent: () => import('./views/pages/authentication/sign-in/sign-in.component').then((c) => c.SignInComponent)
      },
    ]
  },
  {
    path: 'dashboard',
    component: AdminComponent,
    canActivate: [AdminGuard, AuthGuard],
    children: [
      {
        path: '',
        redirectTo: 'analytics',
        pathMatch: 'full'
      },
      {
        path: 'analytics',
        loadComponent: () => import('./views/dashboard/dash-analytics.component').then((c) => c.DashAnalyticsComponent)
      }
    ]
  },
  { path: '**', redirectTo: '404' },
  {
    path: '404',
    component: P404Component,
    data: { title: 'Page 404' },
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule],
  providers: [
    provideRouter(routes, withHashLocation()),
    { provide: LocationStrategy, useClass: HashLocationStrategy },

  ]
})
export class AppRoutingModule { }
