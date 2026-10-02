import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { AuthService } from '../../../../services/auth-service/auth.service';
import UserLoginCredentials from '../../../../interfaces/UserLoginCredentials';
import { errorMessage, fieldErrors } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { BoAuthShellComponent } from '../../shared/bo-auth-shell.component';

/**
 * Connexion d'un administrateur de la plateforme (POST /api/platform/admin/login).
 * En cas de succès, le jeton est enregistré puis on redirige vers /backoffice/dashboard.
 */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, LucideDynamicIcon, BoAuthShellComponent],
  template: `
    <bo-auth-shell title="Connexion" subtitle="Accès réservé aux administrateurs de la plateforme.">
      <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="space-y-5" novalidate>
        @if (errorMessage()) {
          <div class="alert-error flex items-start gap-2" role="alert">
            <svg [lucideIcon]="icons.TriangleAlert" [size]="18" class="shrink-0 mt-0.5" />
            <span>{{ errorMessage() }}</span>
          </div>
        }

        <div>
          <label class="field-label" for="email">Adresse e-mail</label>
          <input id="email" class="field-input" type="email" formControlName="email" autocomplete="email"
                 placeholder="nom@drivehub.cm" />
          @if (serverErrors()['email']) {
            <p class="bo-field-error">{{ serverErrors()['email'] }}</p>
          } @else if (loginForm.controls.email.invalid && loginForm.controls.email.touched) {
            <p class="bo-field-error">Saisissez une adresse e-mail valide.</p>
          }
        </div>

        <div>
          <label class="field-label" for="password">Mot de passe</label>
          <div class="relative">
            <input id="password" class="field-input pr-12!" [type]="showPassword() ? 'text' : 'password'"
                   formControlName="password" autocomplete="current-password" />
            <button type="button" class="bo-icon-btn absolute right-0.5 top-1/2 -translate-y-1/2"
                    (click)="showPassword.set(!showPassword())"
                    [attr.aria-label]="showPassword() ? 'Masquer le mot de passe' : 'Afficher le mot de passe'">
              <svg [lucideIcon]="showPassword() ? icons.EyeOff : icons.Eye" [size]="18" />
            </button>
          </div>
          @if (serverErrors()['password']) {
            <p class="bo-field-error">{{ serverErrors()['password'] }}</p>
          } @else if (loginForm.controls.password.invalid && loginForm.controls.password.touched) {
            <p class="bo-field-error">Le mot de passe est requis.</p>
          }
        </div>

        <button type="submit" class="btn-primary w-full min-h-12" [disabled]="isLoading()">
          {{ isLoading() ? 'Connexion...' : 'Se connecter' }}
        </button>

        <p class="text-sm text-center text-black/50 dark:text-white/50">
          Pas encore de compte ?
          <a routerLink="/backoffice/register" class="inline-block py-2 text-[#0070f3] font-semibold">Demander un accès</a>
        </p>
      </form>
    </bo-auth-shell>
  `,
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  protected readonly icons = ICONS;

  errorMessage = signal<string>('');
  /** Erreurs par champ renvoyées par le backend (fieldErrors), vides par défaut. */
  serverErrors = signal<Record<string, string>>({});
  isLoading = signal<boolean>(false);
  showPassword = signal<boolean>(false);

  loginForm = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  onSubmit(): void {
    // Formulaire incomplet : on affiche les messages sous les champs au lieu d'appeler l'API
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    this.serverErrors.set({});

    // Objet envoyé à l'API
    const credentials: UserLoginCredentials = {
      email: this.loginForm.controls.email.value.trim(),
      password: this.loginForm.controls.password.value,
    };

    // La requête HTTP part réellement au moment du subscribe()
    this.authService.login(credentials).subscribe({
      next: (response) => {
        this.authService.saveToken(response.token);
        this.router.navigate(['/backoffice/dashboard']);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.serverErrors.set(fieldErrors(err));
        // Le message précis du backend est prioritaire ; à défaut, un message selon le code HTTP
        this.errorMessage.set(errorMessage(err, this.fallbackFor(err?.status)));
      },
    });
  }

  /** Message utilisé seulement si le backend ne renvoie pas de « message ». */
  private fallbackFor(status: number | undefined): string {
    switch (status) {
      case 401: return 'Email ou mot de passe incorrect.';
      case 403: return 'Votre compte est en attente de validation par un administrateur.';
      case 404: return 'Aucun compte associé à cet email.';
      default: return 'Une erreur est survenue. Réessayez.';
    }
  }
}
