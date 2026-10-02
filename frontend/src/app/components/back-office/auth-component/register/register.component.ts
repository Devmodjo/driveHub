import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { AuthService } from '../../../../services/auth-service/auth.service';
import { Role } from '../../../../enums/role.enum';
import UserRegisterModel from '../../../../interfaces/UserRegisterModel';
import { errorMessage, fieldErrors } from '../../../../shared/http-error';
import { ICONS } from '../../../../shared/icons';
import { BoAuthShellComponent } from '../../shared/bo-auth-shell.component';
import { ModalComponent } from '../modal/modal.component';

/**
 * Demande de création d'un compte administrateur (POST /api/platform/admin/register).
 * Le compte reste en attente jusqu'à sa validation par un ROOT ; une fenêtre de confirmation
 * rappelle alors le mot de passe provisoire choisi.
 */
@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, LucideDynamicIcon, BoAuthShellComponent, ModalComponent],
  templateUrl: './register.html',
})
export class RegisterComponent {
  private authService = inject(AuthService);
  protected readonly icons = ICONS;
  protected readonly role = Role;

  /** Vrai quand la demande a été acceptée par l'API : affiche la fenêtre de confirmation. */
  isSend = signal<boolean>(false);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string>('');
  /** Erreurs par champ renvoyées par le backend (fieldErrors). */
  serverErrors = signal<Record<string, string>>({});

  registerForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    role: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(8)] }),
    residence: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    phoneNumber: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern('^[0-9]+$')],
    }),
    reason: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  /** Vrai si le champ doit afficher son erreur locale (invalide et déjà visité). */
  showError(field: string): boolean {
    const control = this.registerForm.get(field);
    return !!control && control.invalid && control.touched;
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const value = this.registerForm.getRawValue();
    // Objet envoyé à l'API
    const registerCredential: UserRegisterModel = { ...value, email: value.email.trim(), role: value.role as Role };

    this.isLoading.set(true);
    this.errorMessage.set('');
    this.serverErrors.set({});

    this.authService.register(registerCredential).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.isSend.set(true);
      },
      error: (error) => {
        this.isLoading.set(false);
        this.isSend.set(false);
        this.serverErrors.set(fieldErrors(error));
        const fallback = error?.status === 409
          ? 'Cette adresse e-mail est déjà enregistrée.'
          : 'Les informations saisies sont incorrectes. Vérifiez le formulaire.';
        this.errorMessage.set(errorMessage(error, fallback));
      },
    });
  }
}
