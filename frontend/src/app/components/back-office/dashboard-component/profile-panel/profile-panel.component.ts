import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../../services/admin-service/admin.service';
import { AdminProfile } from '../../../../interfaces/AdminProfile';
import { errorMessage, fieldErrorsOf } from '../../../../shared/http-error';
import { BoDrawerComponent } from '../../shared/bo-drawer.component';
import { adminStatusClass, adminStatusLabel, initials, roleLabel } from '../../shared/bo-status';

/**
 * Volet « Mon profil » de l'administrateur connecté :
 *  - modification de ses informations (PATCH /api/platform/admin/me) ;
 *  - changement de son mot de passe (PATCH /api/platform/admin/me/password).
 * Ouvert depuis la barre latérale ; émet (updated) avec le profil à jour après un enregistrement.
 */
@Component({
  selector: 'bo-profile-panel',
  imports: [FormsModule, BoDrawerComponent],
  template: `
    <bo-drawer title="Mon profil" (closed)="closed.emit()">
      <!-- Identité -->
      <div class="flex items-center gap-4 mb-6">
        <div class="w-14 h-14 shrink-0 rounded-2xl bg-[#0070f3] text-white flex items-center justify-center text-lg font-black">
          {{ initials(admin().name) }}
        </div>
        <div class="min-w-0">
          <p class="font-bold text-black dark:text-white truncate">{{ admin().name }}</p>
          <p class="text-sm text-black/50 dark:text-white/50 truncate">{{ admin().email }}</p>
          <div class="mt-1.5 flex flex-wrap gap-1.5">
            <span class="badge bg-[#0070f3]/10 text-[#0070f3]">{{ roleLabel(admin().role) }}</span>
            <span [class]="adminStatusClass(admin().adminStatus)">{{ adminStatusLabel(admin().adminStatus) }}</span>
          </div>
        </div>
      </div>

      <!-- Informations personnelles -->
      <form class="space-y-4" (ngSubmit)="saveProfile()">
        <h4 class="bo-section-title">Mes informations</h4>
        @if (profileError()) { <div class="alert-error" role="alert">{{ profileError() }}</div> }
        @if (profileSuccess()) { <div class="alert-success" role="status">{{ profileSuccess() }}</div> }
        <div>
          <label class="field-label" for="p-name">Nom complet</label>
          <input id="p-name" class="field-input" name="name" [(ngModel)]="name" required autocomplete="name" />
          @if (profileFieldErrors()['name']) { <p class="bo-field-error">{{ profileFieldErrors()['name'] }}</p> }
        </div>
        <div>
          <label class="field-label" for="p-email">Email</label>
          <input id="p-email" class="field-input" type="email" name="email" [(ngModel)]="email" required autocomplete="email" />
          @if (profileFieldErrors()['email']) { <p class="bo-field-error">{{ profileFieldErrors()['email'] }}</p> }
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="field-label" for="p-residence">Résidence</label>
            <input id="p-residence" class="field-input" name="residence" [(ngModel)]="residence" required />
            @if (profileFieldErrors()['residence']) { <p class="bo-field-error">{{ profileFieldErrors()['residence'] }}</p> }
          </div>
          <div>
            <label class="field-label" for="p-phone">Téléphone</label>
            <input id="p-phone" class="field-input" type="tel" name="phoneNumber" [(ngModel)]="phoneNumber" autocomplete="tel" />
            @if (profileFieldErrors()['phoneNumber']) { <p class="bo-field-error">{{ profileFieldErrors()['phoneNumber'] }}</p> }
          </div>
        </div>
        <button type="submit" class="bo-btn bo-btn-blue w-full" [disabled]="savingProfile() || !name.trim() || !email.trim() || !residence.trim()">
          {{ savingProfile() ? 'Enregistrement...' : 'Enregistrer mes informations' }}
        </button>
      </form>

      <!-- Mot de passe -->
      <form class="space-y-4 mt-8 pt-6 border-t border-black/5 dark:border-white/10" (ngSubmit)="savePassword()">
        <h4 class="bo-section-title">Changer de mot de passe</h4>
        @if (passwordError()) { <div class="alert-error" role="alert">{{ passwordError() }}</div> }
        @if (passwordSuccess()) { <div class="alert-success" role="status">{{ passwordSuccess() }}</div> }
        <div>
          <label class="field-label" for="p-old">Mot de passe actuel</label>
          <input id="p-old" class="field-input" type="password" name="oldPassword" [(ngModel)]="oldPassword" autocomplete="current-password" />
          @if (passwordFieldErrors()['oldPassword']) { <p class="bo-field-error">{{ passwordFieldErrors()['oldPassword'] }}</p> }
        </div>
        <div>
          <label class="field-label" for="p-new">Nouveau mot de passe</label>
          <input id="p-new" class="field-input" type="password" name="newPassword" [(ngModel)]="newPassword" autocomplete="new-password" />
          @if (passwordFieldErrors()['newPassword']) { <p class="bo-field-error">{{ passwordFieldErrors()['newPassword'] }}</p> }
          @else { <p class="mt-1.5 text-xs text-black/50 dark:text-white/50">Au moins 8 caractères.</p> }
        </div>
        <div>
          <label class="field-label" for="p-confirm">Confirmer le nouveau mot de passe</label>
          <input id="p-confirm" class="field-input" type="password" name="confirmPassword" [(ngModel)]="confirmPassword" autocomplete="new-password" />
          @if (confirmPassword && confirmPassword !== newPassword) {
            <p class="bo-field-error">Les deux mots de passe ne correspondent pas.</p>
          }
        </div>
        <button type="submit" class="bo-btn bo-btn-outline w-full"
                [disabled]="savingPassword() || !oldPassword || newPassword.length < 8 || newPassword !== confirmPassword">
          {{ savingPassword() ? 'Modification...' : 'Modifier le mot de passe' }}
        </button>
      </form>
    </bo-drawer>
  `,
})
export class ProfilePanelComponent implements OnInit {
  private readonly adminService = inject(AdminService);

  readonly admin = input.required<AdminProfile>();
  readonly closed = output<void>();
  readonly updated = output<AdminProfile>();

  // Fonctions de présentation exposées au gabarit
  protected readonly initials = initials;
  protected readonly roleLabel = roleLabel;
  protected readonly adminStatusLabel = adminStatusLabel;
  protected readonly adminStatusClass = adminStatusClass;

  // Champs du formulaire « Mes informations » (pré-remplis à l'ouverture)
  protected name = '';
  protected email = '';
  protected residence = '';
  protected phoneNumber = '';
  protected readonly savingProfile = signal(false);
  protected readonly profileError = signal('');
  protected readonly profileSuccess = signal('');
  protected readonly profileFieldErrors = signal<Record<string, string>>({});

  // Champs du formulaire « Mot de passe »
  protected oldPassword = '';
  protected newPassword = '';
  protected confirmPassword = '';
  protected readonly savingPassword = signal(false);
  protected readonly passwordError = signal('');
  protected readonly passwordSuccess = signal('');
  protected readonly passwordFieldErrors = signal<Record<string, string>>({});

  ngOnInit(): void {
    const admin = this.admin();
    this.name = admin.name ?? '';
    this.email = admin.email ?? '';
    this.residence = admin.residence ?? '';
    this.phoneNumber = admin.phoneNumber ?? '';
  }

  protected saveProfile(): void {
    this.savingProfile.set(true);
    this.profileError.set('');
    this.profileSuccess.set('');
    this.profileFieldErrors.set({});
    // Le backend exige le rôle : on renvoie le rôle actuel (on ne peut pas changer son propre rôle ici)
    this.adminService.updateCurrentAdmin({
      name: this.name.trim(),
      email: this.email.trim(),
      role: this.admin().role,
      residence: this.residence.trim(),
      phoneNumber: this.phoneNumber.trim(),
    }).subscribe({
      next: (profile) => {
        this.savingProfile.set(false);
        this.profileSuccess.set('Vos informations ont été enregistrées.');
        this.updated.emit(profile);
      },
      error: (err) => {
        this.savingProfile.set(false);
        this.profileFieldErrors.set(fieldErrorsOf(err));
        this.profileError.set(errorMessage(err, 'Impossible d\'enregistrer vos informations.'));
      },
    });
  }

  protected savePassword(): void {
    this.savingPassword.set(true);
    this.passwordError.set('');
    this.passwordSuccess.set('');
    this.passwordFieldErrors.set({});
    this.adminService.changePassword({ oldPassword: this.oldPassword, newPassword: this.newPassword }).subscribe({
      next: (res) => {
        this.savingPassword.set(false);
        this.passwordSuccess.set(res?.message || 'Mot de passe modifié.');
        this.oldPassword = this.newPassword = this.confirmPassword = '';
      },
      error: (err) => {
        this.savingPassword.set(false);
        this.passwordFieldErrors.set(fieldErrorsOf(err));
        this.passwordError.set(errorMessage(err, 'Impossible de modifier le mot de passe.'));
      },
    });
  }
}
