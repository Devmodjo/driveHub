import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage } from '../../../../shared/http-error';
import { AuthShellComponent } from './auth-shell.component';

/** Lien reçu par email : /reset-password?token=... */
@Component({
  selector: 'app-reset-password',
  imports: [FormsModule, RouterLink, AuthShellComponent],
  template: `
    <app-auth-shell title="Nouveau mot de passe">
      @if (message()) {
        <div class="alert-success mb-6">{{ message() }}</div>
        <a routerLink="/connexion" class="btn-primary">Se connecter</a>
      } @else {
        <form class="space-y-5" (ngSubmit)="submit()">
          @if (error()) { <div class="alert-error">{{ error() }}</div> }
          <div>
            <label class="field-label" for="password">Nouveau mot de passe (8 caractères minimum)</label>
            <input id="password" class="field-input" type="password" name="password" minlength="8" required autocomplete="new-password" [(ngModel)]="password" />
          </div>
          <button type="submit" class="btn-primary w-full" [disabled]="loading() || password.length < 8">Enregistrer</button>
        </form>
      }
    </app-auth-shell>
  `,
})
export class ResetPasswordComponent {
  private readonly session = inject(SessionService);
  private readonly token = inject(ActivatedRoute).snapshot.queryParamMap.get('token') ?? '';
  protected password = '';
  protected readonly loading = signal(false);
  protected readonly message = signal('');
  protected readonly error = signal('');

  protected submit(): void {
    this.loading.set(true);
    this.error.set('');
    this.session.resetPassword(this.token, this.password).subscribe({
      next: (res) => { this.loading.set(false); this.message.set(res.message); },
      error: (err) => { this.loading.set(false); this.error.set(errorMessage(err, 'Lien invalide ou expiré.')); },
    });
  }
}
