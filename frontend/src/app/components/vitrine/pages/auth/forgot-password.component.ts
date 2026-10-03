import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage } from '../../../../shared/http-error';
import { SpamHintComponent } from '../../../../shared/spam-hint.component';
import { AuthShellComponent } from './auth-shell.component';

/** Demande d'un lien de réinitialisation du mot de passe. */
@Component({
  selector: 'app-forgot-password',
  imports: [FormsModule, RouterLink, AuthShellComponent, SpamHintComponent],
  template: `
    <app-auth-shell title="Mot de passe oublié" subtitle="Saisissez votre email : vous recevrez un lien pour choisir un nouveau mot de passe.">
      <form class="space-y-5" (ngSubmit)="submit()">
        @if (message()) {
          <div>
            <div class="alert-success">{{ message() }}</div>
            <app-spam-hint class="mt-2" />
          </div>
        }
        @if (error()) { <div class="alert-error">{{ error() }}</div> }
        <div>
          <label class="field-label" for="email">Email</label>
          <input id="email" class="field-input" type="email" name="email" required [(ngModel)]="email" />
        </div>
        <button type="submit" class="btn-primary w-full" [disabled]="loading() || !email">Envoyer le lien</button>
        <p class="text-sm text-center"><a routerLink="/connexion" class="text-[#0070f3] font-semibold">Retour à la connexion</a></p>
      </form>
    </app-auth-shell>
  `,
})
export class ForgotPasswordComponent {
  private readonly session = inject(SessionService);
  protected email = '';
  protected readonly loading = signal(false);
  protected readonly message = signal('');
  protected readonly error = signal('');

  protected submit(): void {
    this.loading.set(true);
    this.error.set('');
    this.session.forgotPassword(this.email.trim()).subscribe({
      next: (res) => { this.loading.set(false); this.message.set(res.message); },
      error: (err) => { this.loading.set(false); this.error.set(errorMessage(err)); },
    });
  }
}
