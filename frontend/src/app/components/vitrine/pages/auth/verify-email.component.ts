import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiMessage } from '../../../../interfaces/drivehub.models';
import { errorMessage } from '../../../../shared/http-error';
import { API_URL, BASE_URL } from '../../../../utils/UTILS';
import { AuthShellComponent } from './auth-shell.component';

/**
 * Lien reçu par email : /verify-email?token=... (moniteurs, élèves)
 * ou /backoffice/verify-email?token=... (administrateurs de la plateforme, route data "admin").
 */
@Component({
  selector: 'app-verify-email',
  imports: [RouterLink, AuthShellComponent],
  template: `
    <app-auth-shell title="Vérification de l'email">
      @if (status() === 'pending') { <p class="text-black/60 dark:text-white/60">Vérification en cours...</p> }
      @if (status() === 'ok') { <div class="alert-success mb-6">{{ message() }}</div> }
      @if (status() === 'error') { <div class="alert-error mb-6">{{ message() }}</div> }
      @if (status() !== 'pending') {
        <a [routerLink]="admin ? '/backoffice/login' : '/connexion'" class="btn-primary">Aller à la connexion</a>
      }
    </app-auth-shell>
  `,
})
export class VerifyEmailComponent {
  private readonly route = inject(ActivatedRoute);
  protected readonly admin = this.route.snapshot.data['admin'] === true;
  protected readonly status = signal<'pending' | 'ok' | 'error'>('pending');
  protected readonly message = signal('');

  constructor() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.status.set('error');
      this.message.set('Lien invalide : jeton manquant.');
      return;
    }
    const url = this.admin ? `${BASE_URL}admin/verify-email` : `${API_URL}auth/verify-email`;
    inject(HttpClient).get<ApiMessage>(url, { params: { token } }).subscribe({
      next: (res) => { this.status.set('ok'); this.message.set(res.message || 'Email vérifié.'); },
      error: (err) => { this.status.set('error'); this.message.set(errorMessage(err, 'Lien invalide ou expiré.')); },
    });
  }
}
