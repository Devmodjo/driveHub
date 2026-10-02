import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { RegisterRequest, UserRole } from '../../../../interfaces/drivehub.models';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage } from '../../../../shared/http-error';
import { AuthShellComponent } from './auth-shell.component';

/**
 * Inscription d'un élève ou d'un moniteur (POST /api/auth/register/student|monitor).
 * Le compte est créé dans le schéma public ; un email de vérification est envoyé.
 * Ensuite : le moniteur demande la création de son auto-école, l'élève rejoint une auto-école.
 */
@Component({
  selector: 'app-user-register',
  imports: [FormsModule, RouterLink, AuthShellComponent],
  template: `
    <app-auth-shell badge="Espace auto-école" title="Créer un compte" [wide]="true"
                    subtitle="Un seul compte pour suivre votre formation ou gérer votre auto-école.">
      @if (done()) {
        <div class="alert-success mb-6">{{ done() }}</div>
        <p class="text-black/60 dark:text-white/60 font-light mb-8">
          Ouvrez l'email que nous venons d'envoyer à <strong>{{ form.email }}</strong> et cliquez sur le lien de vérification,
          puis connectez-vous.
        </p>
        <a routerLink="/connexion" class="btn-primary">Aller à la connexion</a>
      } @else {
        <form class="space-y-6" (ngSubmit)="submit()">
          <div class="grid grid-cols-2 gap-3 p-1 rounded-2xl bg-black/5 dark:bg-white/5">
            <button type="button" class="py-3 rounded-xl font-bold transition-colors"
                    [class]="role() === 'STUDENT' ? 'bg-white dark:bg-black text-[#0070f3] shadow-sm' : 'text-black/50 dark:text-white/50'"
                    (click)="role.set('STUDENT')">Je suis élève</button>
            <button type="button" class="py-3 rounded-xl font-bold transition-colors"
                    [class]="role() === 'MONITOR' ? 'bg-white dark:bg-black text-[#0070f3] shadow-sm' : 'text-black/50 dark:text-white/50'"
                    (click)="role.set('MONITOR')">Je dirige une auto-école</button>
          </div>

          @if (error()) { <div class="alert-error">{{ error() }}</div> }

          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div><label class="field-label" for="firstname">Prénom</label>
              <input id="firstname" class="field-input" name="firstname" required [(ngModel)]="form.firstname" /></div>
            <div><label class="field-label" for="lastname">Nom</label>
              <input id="lastname" class="field-input" name="lastname" [(ngModel)]="form.lastname" /></div>
            <div><label class="field-label" for="email">Email</label>
              <input id="email" class="field-input" type="email" name="email" required [(ngModel)]="form.email" /></div>
            <div><label class="field-label" for="phone">Téléphone</label>
              <input id="phone" class="field-input" name="phone" placeholder="+237 6XX XX XX XX" required [(ngModel)]="form.phoneNumber" /></div>
            <div><label class="field-label" for="gender">Genre</label>
              <select id="gender" class="field-input" name="gender" [(ngModel)]="form.gender">
                <option value="MALE">Homme</option>
                <option value="FEMALE">Femme</option>
              </select></div>
            <div><label class="field-label" for="birth">Date de naissance</label>
              <input id="birth" class="field-input" type="date" name="birth" required [(ngModel)]="form.dateOfBirth" /></div>
            <div><label class="field-label" for="nationality">Nationalité</label>
              <input id="nationality" class="field-input" name="nationality" required [(ngModel)]="form.nationality" /></div>
            <div><label class="field-label" for="city">Ville de résidence</label>
              <input id="city" class="field-input" name="city" required [(ngModel)]="form.residenceCity" /></div>
            <div class="md:col-span-2"><label class="field-label" for="password">Mot de passe (8 caractères minimum)</label>
              <input id="password" class="field-input" type="password" name="password" minlength="8" required autocomplete="new-password" [(ngModel)]="form.password" /></div>
          </div>

          <button type="submit" class="btn-primary w-full" [disabled]="loading()">
            {{ loading() ? 'Création...' : 'Créer mon compte' }}
          </button>
          <p class="text-sm text-center text-black/50 dark:text-white/50">
            Déjà inscrit ? <a routerLink="/connexion" class="text-[#0070f3] font-semibold">Se connecter</a>
          </p>
        </form>
      }
    </app-auth-shell>
  `,
})
export class UserRegisterComponent {
  private readonly session = inject(SessionService);

  protected readonly role = signal<UserRole>(
    inject(ActivatedRoute).snapshot.queryParamMap.get('role') === 'MONITOR' ? 'MONITOR' : 'STUDENT',
  );
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly done = signal('');

  protected form: RegisterRequest = {
    firstname: '', lastname: '', email: '', password: '', phoneNumber: '',
    gender: 'MALE', nationality: 'Camerounaise', residenceCity: '', dateOfBirth: '',
  };

  protected submit(): void {
    this.loading.set(true);
    this.error.set('');
    this.session.register(this.role(), { ...this.form, email: this.form.email.trim() }).subscribe({
      next: (res) => { this.loading.set(false); this.done.set(res.message); },
      error: (err) => { this.loading.set(false); this.error.set(errorMessage(err)); },
    });
  }
}
