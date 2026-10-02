import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage, fieldErrorsOf } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { AuthShellComponent } from './auth-shell.component';

/**
 * Lien reçu par email par un moniteur ajouté par le responsable de son auto-école :
 * /invitation?token=... (valable 72 heures).
 *
 * Le moniteur choisit son mot de passe et accepte la politique de confidentialité
 * (même texte que sur la page d'inscription), puis il peut se connecter.
 */
@Component({
  selector: 'app-accept-invitation',
  imports: [FormsModule, RouterLink, LucideDynamicIcon, AuthShellComponent],
  template: `
    <app-auth-shell badge="Invitation" title="Rejoignez votre auto‑école"
                    subtitle="Votre auto-école vous a ajouté comme moniteur sur DriveHub. Choisissez votre mot de passe pour activer votre compte.">
      @if (done()) {
        <div class="alert-success mb-6" role="status">{{ done() }}</div>
        <a routerLink="/connexion" class="btn-primary w-full">Se connecter</a>
      } @else if (!token) {
        <div class="alert-error mb-6" role="alert">
          Ce lien d'invitation est incomplet. Ouvrez à nouveau le lien reçu par email, ou demandez au responsable de votre auto-école de vous renvoyer une invitation.
        </div>
        <a routerLink="/" class="btn-ghost">Retour à l'accueil</a>
      } @else {
        @let e = fieldErrors();
        <form class="space-y-5" (ngSubmit)="submit()" novalidate>
          @if (error()) { <div class="alert-error" role="alert">{{ error() }}</div> }
          <div>
            <label class="field-label" for="password">Mot de passe (8 caractères minimum)</label>
            <div class="relative">
              <input id="password" class="field-input pr-12" [class.field-input-error]="e['password']" [type]="showPassword() ? 'text' : 'password'"
                     name="password" minlength="8" required autocomplete="new-password" [(ngModel)]="password" />
              <button type="button" class="absolute right-2 top-1/2 -translate-y-1/2 p-2 text-black/50 dark:text-white/50"
                      [attr.aria-label]="showPassword() ? 'Masquer le mot de passe' : 'Afficher le mot de passe'"
                      (click)="showPassword.set(!showPassword())">
                <svg [lucideIcon]="showPassword() ? icons.EyeOff : icons.Eye" [size]="18" />
              </button>
            </div>
            @if (e['password']) { <p class="field-error">{{ e['password'] }}</p> }
          </div>
          <div>
            <label class="field-label" for="confirm">Confirmez le mot de passe</label>
            <input id="confirm" class="field-input" [class.field-input-error]="mismatch()" [type]="showPassword() ? 'text' : 'password'"
                   name="confirm" required autocomplete="new-password" [(ngModel)]="confirm" (blur)="touched.set(true)" />
            @if (mismatch()) { <p class="field-error">Les deux mots de passe ne correspondent pas.</p> }
          </div>

          <!-- Consentement obligatoire (loi n° 2024/017 sur la protection des données personnelles), même texte qu'à l'inscription -->
          <div class="rounded-2xl border p-4" [class]="e['acceptPrivacyPolicy'] ? 'border-red-500/60' : 'border-black/10 dark:border-white/10'">
            <label class="flex items-start gap-3 cursor-pointer">
              <input type="checkbox" name="acceptPrivacyPolicy" class="mt-1 h-5 w-5 shrink-0 accent-[#0070f3]" [(ngModel)]="accepted" />
              <span class="text-sm text-black/70 dark:text-white/70 leading-relaxed">
                J'accepte la <a routerLink="/confidentialite" target="_blank" class="text-[#0070f3] font-semibold underline">politique de confidentialité</a>.
                J'autorise DriveHub à collecter mes informations pour vérifier mon identité et la validité de mon auto-école.
                Elles ne sont utilisées que dans ce cadre professionnel.
              </span>
            </label>
            @if (e['acceptPrivacyPolicy']) { <p class="field-error">{{ e['acceptPrivacyPolicy'] }}</p> }
          </div>

          <button type="submit" class="btn-primary w-full" [disabled]="loading() || !canSubmit()">
            {{ loading() ? 'Activation...' : 'Activer mon compte' }}
          </button>
        </form>
      }
    </app-auth-shell>
  `,
})
export class AcceptInvitationComponent {
  protected readonly icons = ICONS;
  private readonly session = inject(SessionService);
  protected readonly token = inject(ActivatedRoute).snapshot.queryParamMap.get('token') ?? '';

  protected password = '';
  protected confirm = '';
  protected accepted = false;
  protected readonly showPassword = signal(false);
  /** Le champ de confirmation a été quitté : on peut signaler une différence. */
  protected readonly touched = signal(false);
  protected readonly loading = signal(false);
  protected readonly done = signal('');
  protected readonly error = signal('');
  protected readonly fieldErrors = signal<Record<string, string>>({});

  protected mismatch(): boolean {
    return this.touched() && this.confirm.length > 0 && this.confirm !== this.password;
  }

  protected canSubmit(): boolean {
    return this.password.length >= 8 && this.password === this.confirm && this.accepted;
  }

  protected submit(): void {
    if (!this.canSubmit()) return;
    this.loading.set(true);
    this.error.set('');
    this.fieldErrors.set({});
    this.session.acceptInvitation({ token: this.token, password: this.password, acceptPrivacyPolicy: this.accepted }).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.done.set(res?.message || 'Votre compte est activé. Vous pouvez maintenant vous connecter.');
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(errorMessage(err, 'Lien invalide ou expiré : demandez une nouvelle invitation à votre auto-école.'));
        this.fieldErrors.set(fieldErrorsOf(err));
      },
    });
  }
}
