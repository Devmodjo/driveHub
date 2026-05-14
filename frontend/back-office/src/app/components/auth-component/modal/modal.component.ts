import { Component, Input, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'register-modal',
  imports: [],
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.css',
})
export class ModalComponent {

  @Input() title?: string;
  @Input() message?: string;
  @Input() password?: string; // On récupère le mot de passe

  private router = inject(Router);

  isPasswordVisible = false;
  copySuccess = false;

  togglePassword() {
    this.isPasswordVisible = !this.isPasswordVisible;
  }

  copyPassword() {
    if (this.password) {
      navigator.clipboard.writeText(this.password);
      this.copySuccess = true;
      // Remet le bouton à l'état normal après 2 secondes
      setTimeout(() => this.copySuccess = false, 2000);
    }
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
