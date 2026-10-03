import { Component, Input, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { ICONS } from '../../../../shared/icons';
import { SpamHintComponent } from '../../../../shared/spam-hint.component';

/**
 * Fenêtre affichée après une demande d'inscription réussie.
 * Elle annonce l'email de décision (avec le conseil « regardez dans vos spams »),
 * rappelle le mot de passe provisoire (masqué par défaut, copiable) puis renvoie vers la connexion.
 *
 * Mobile : feuille collée en bas de l'écran ; à partir de sm: : fenêtre centrée.
 */
@Component({
  selector: 'register-modal',
  imports: [LucideDynamicIcon, SpamHintComponent],
  templateUrl: './modal.component.html',
})
export class ModalComponent {
  @Input() title?: string;
  @Input() message?: string;
  /** Mot de passe provisoire saisi dans le formulaire d'inscription. */
  @Input() password?: string;

  private router = inject(Router);
  protected readonly icons = ICONS;

  // Signaux : l'application est « zoneless », un simple champ modifié dans un setTimeout
  // ne rafraîchirait pas l'affichage.
  isPasswordVisible = signal(false);
  copySuccess = signal(false);

  togglePassword(): void {
    this.isPasswordVisible.update((visible) => !visible);
  }

  copyPassword(): void {
    if (!this.password) {
      return;
    }
    navigator.clipboard?.writeText(this.password).then(
      () => {
        this.copySuccess.set(true);
        // Le bouton revient à son état normal après 2 secondes
        setTimeout(() => this.copySuccess.set(false), 2000);
      },
      () => undefined,
    );
  }

  goToLogin(): void {
    this.router.navigate(['/backoffice/login']);
  }
}
