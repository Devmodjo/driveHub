import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage } from '../../../../shared/http-error';
import { AuthShellComponent } from './auth-shell.component';

/** Connexion des moniteurs et des élèves (POST /api/auth/login). */
@Component({
  selector: 'app-user-login',
  imports: [FormsModule, RouterLink, AuthShellComponent],
  template: `
    <app-auth-shell badge="Espace auto-école" title="Connexion" subtitle="Moniteurs et élèves : accédez à votre espace.">
      <form class="space-y-5" (ngSubmit)="submit()">
        @if (error()) { <div class="alert-error">{{ error() }}</div> }
        <div>
          <label class="field-label" for="email">Email</label>
          <input id="email" class="field-input" type="email" name="email" autocomplete="email" required [(ngModel)]="email" />
        </div>
        <div>
          <label class="field-label" for="password">Mot de passe</label>
          <input id="password" class="field-input" type="password" name="password" autocomplete="current-password" required [(ngModel)]="password" />
        </div>
        <div class="text-right">
          <a routerLink="/mot-de-passe-oublie" class="text-sm text-[#0070f3] font-semibold">Mot de passe oublié ?</a>
        </div>
        <button type="submit" class="btn-primary w-full" [disabled]="loading() || !email || !password">
          {{ loading() ? 'Connexion...' : 'Se connecter' }}
        </button>
        <p class="text-sm text-center text-black/50 dark:text-white/50">
          Pas encore de compte ? <a routerLink="/inscription" class="text-[#0070f3] font-semibold">Créer un compte</a>
        </p>
      </form>
    </app-auth-shell>
  `,
})
export class UserLoginComponent {
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected email = '';
  protected password = '';
  protected readonly loading = signal(false);
  protected readonly error = signal('');

  protected submit(): void {
    this.loading.set(true);
    this.error.set('');
    this.session.login(this.email.trim(), this.password).subscribe({
      next: () => {
        const redirect = this.route.snapshot.queryParamMap.get('redirect');
        this.router.navigateByUrl(redirect && redirect.startsWith('/') ? redirect : '/dashboard');
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(errorMessage(err, 'Email ou mot de passe incorrect.'));
      },
    });
  }
}
