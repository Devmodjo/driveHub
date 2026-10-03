import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { RegisterRequest, UserRole } from '../../../../interfaces/drivehub.models';
import { SessionService } from '../../../../services/session-service/session.service';
import { errorMessage, fieldErrorsOf } from '../../../../shared/http-error';
import { SpamHintComponent } from '../../../../shared/spam-hint.component';
import { AuthShellComponent } from './auth-shell.component';

/**
 * Inscription d'un élève ou d'un moniteur (POST /api/auth/register/student|monitor).
 *
 * - Le compte est créé dans le schéma public ; un email de vérification est envoyé.
 * - L'acceptation de la politique de confidentialité est obligatoire (case à cocher) :
 *   le bouton reste inactif tant qu'elle n'est pas cochée, et le backend refuse aussi l'inscription sans elle.
 * - Une erreur de saisie renvoyée par le backend s'affiche sous le champ concerné (fieldErrors).
 * - Les limites de longueur (maxlength) sont les mêmes que celles du backend.
 *
 * Ensuite : le moniteur enregistre son auto-école, l'élève choisit une auto-école.
 */
@Component({
  selector: 'app-user-register',
  imports: [FormsModule, RouterLink, AuthShellComponent, SpamHintComponent],
  template: `
    <app-auth-shell badge="Espace auto-école" title="Créer un compte" [wide]="true"
                    subtitle="Un seul compte pour suivre votre formation ou gérer votre auto-école.">
      @if (done()) {
        <div class="alert-success mb-6">{{ done() }}</div>
        <p class="text-black/60 dark:text-white/60 font-light mb-3">
          Ouvrez l'email que nous venons d'envoyer à <strong>{{ form.email }}</strong> et cliquez sur le lien de vérification,
          puis connectez-vous.
        </p>
        <app-spam-hint class="mb-8" />
        <a routerLink="/connexion" class="btn-primary">Aller à la connexion</a>
      } @else {
        @let e = fieldErrors();
        <form class="space-y-6" (ngSubmit)="submit()" novalidate>
          <div class="grid grid-cols-2 gap-3 p-1 rounded-2xl bg-black/5 dark:bg-white/5">
            <button type="button" class="py-3 rounded-xl font-bold transition-colors"
                    [class]="role() === 'STUDENT' ? 'bg-white dark:bg-black text-[#0070f3] shadow-sm' : 'text-black/50 dark:text-white/50'"
                    (click)="role.set('STUDENT')">Je suis élève</button>
            <button type="button" class="py-3 rounded-xl font-bold transition-colors"
                    [class]="role() === 'MONITOR' ? 'bg-white dark:bg-black text-[#0070f3] shadow-sm' : 'text-black/50 dark:text-white/50'"
                    (click)="role.set('MONITOR')">Je dirige une auto-école</button>
          </div>

          @if (error()) { <div class="alert-error" role="alert">{{ error() }}</div> }

          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div>
              <label class="field-label" for="firstname">Prénom</label>
              <input id="firstname" class="field-input" [class.field-input-error]="e['firstname']" name="firstname"
                     maxlength="100" required autocomplete="given-name" [(ngModel)]="form.firstname" />
              @if (e['firstname']) { <p class="field-error">{{ e['firstname'] }}</p> }
            </div>
            <div>
              <label class="field-label" for="lastname">Nom</label>
              <input id="lastname" class="field-input" [class.field-input-error]="e['lastname']" name="lastname"
                     maxlength="100" autocomplete="family-name" [(ngModel)]="form.lastname" />
              @if (e['lastname']) { <p class="field-error">{{ e['lastname'] }}</p> }
            </div>
            <div>
              <label class="field-label" for="email">Email</label>
              <input id="email" class="field-input" [class.field-input-error]="e['email']" type="email" name="email"
                     maxlength="150" required autocomplete="email" [(ngModel)]="form.email" />
              @if (e['email']) { <p class="field-error">{{ e['email'] }}</p> }
            </div>
            <div>
              <label class="field-label" for="phone">Téléphone</label>
              <input id="phone" class="field-input" [class.field-input-error]="e['phoneNumber']" name="phone" type="tel"
                     placeholder="+237 6XX XX XX XX" maxlength="20" required autocomplete="tel" [(ngModel)]="form.phoneNumber" />
              @if (e['phoneNumber']) { <p class="field-error">{{ e['phoneNumber'] }}</p> }
            </div>
            <div>
              <label class="field-label" for="gender">Genre</label>
              <select id="gender" class="field-input" name="gender" [(ngModel)]="form.gender">
                <option value="MALE">Homme</option>
                <option value="FEMALE">Femme</option>
              </select>
              @if (e['gender']) { <p class="field-error">{{ e['gender'] }}</p> }
            </div>
            <div>
              <label class="field-label" for="birth">Date de naissance</label>
              <input id="birth" class="field-input" [class.field-input-error]="e['dateOfBirth']" type="date" name="birth"
                     required autocomplete="bday" [(ngModel)]="form.dateOfBirth" />
              @if (e['dateOfBirth']) { <p class="field-error">{{ e['dateOfBirth'] }}</p> }
            </div>
            <div>
              <label class="field-label" for="nationality">Nationalité</label>
              <input id="nationality" class="field-input" [class.field-input-error]="e['nationality']" name="nationality"
                     maxlength="100" required [(ngModel)]="form.nationality" />
              @if (e['nationality']) { <p class="field-error">{{ e['nationality'] }}</p> }
            </div>
            <div>
              <label class="field-label" for="city">Ville de résidence</label>
              <input id="city" class="field-input" [class.field-input-error]="e['residenceCity']" name="city"
                     maxlength="100" required autocomplete="address-level2" [(ngModel)]="form.residenceCity" />
              @if (e['residenceCity']) { <p class="field-error">{{ e['residenceCity'] }}</p> }
            </div>
            <div class="md:col-span-2">
              <label class="field-label" for="password">Mot de passe (8 caractères minimum)</label>
              <input id="password" class="field-input" [class.field-input-error]="e['password']" type="password" name="password"
                     minlength="8" maxlength="100" required autocomplete="new-password" [(ngModel)]="form.password" />
              @if (e['password']) { <p class="field-error">{{ e['password'] }}</p> }
            </div>
          </div>

          <!-- Consentement obligatoire (loi n° 2024/017 sur la protection des données personnelles) -->
          <div class="rounded-2xl border p-4" [class]="e['acceptPrivacyPolicy'] ? 'border-red-500/60' : 'border-black/10 dark:border-white/10'">
            <label class="flex items-start gap-3 cursor-pointer">
              <input type="checkbox" name="acceptPrivacyPolicy" class="mt-1 h-5 w-5 shrink-0 accent-[#0070f3]"
                     [(ngModel)]="form.acceptPrivacyPolicy" />
              <span class="text-sm text-black/70 dark:text-white/70 leading-relaxed">
                J'accepte la <a routerLink="/confidentialite" target="_blank" class="text-[#0070f3] font-semibold underline">politique de confidentialité</a>.
                J'autorise DriveHub à collecter mes informations pour vérifier mon identité{{ role() === 'MONITOR' ? ' et la validité de mon auto-école' : '' }}.
                Elles ne sont utilisées que dans ce cadre professionnel.
              </span>
            </label>
            @if (e['acceptPrivacyPolicy']) { <p class="field-error">{{ e['acceptPrivacyPolicy'] }}</p> }
          </div>

          <button type="submit" class="btn-primary w-full" [disabled]="loading() || !form.acceptPrivacyPolicy">
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
  /** Message d'erreur de chaque champ, renvoyé par le backend (vide si tout est valide). */
  protected readonly fieldErrors = signal<Record<string, string>>({});

  protected form: RegisterRequest = {
    firstname: '', lastname: '', email: '', password: '', phoneNumber: '',
    gender: 'MALE', nationality: 'Camerounaise', residenceCity: '', dateOfBirth: '',
    acceptPrivacyPolicy: false,
  };

  protected submit(): void {
    this.loading.set(true);
    this.error.set('');
    this.fieldErrors.set({});
    this.session.register(this.role(), { ...this.form, email: this.form.email.trim() }).subscribe({
      next: (res) => { this.loading.set(false); this.done.set(res.message); },
      error: (err) => {
        this.loading.set(false);
        this.error.set(errorMessage(err));
        this.fieldErrors.set(fieldErrorsOf(err));
      },
    });
  }
}
