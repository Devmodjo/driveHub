// angular import
import { Component, HostListener, inject, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators, AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { NavigationService } from 'src/app/services/navigation.service';
import { NotificationService } from 'src/app/services/notification.service';

import { SharedModule } from 'src/app/theme/shared/shared.module';

@Component({
  selector: 'app-sign-in',
  imports: [SharedModule, RouterModule, ReactiveFormsModule],
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.scss']
})
export class SignInComponent implements OnInit {

  public loginForm: FormGroup | undefined;
  public passwordType: string = 'password';
  public isLoading: boolean = false;
  public submitted = false;
  public emailRegex: RegExp = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

  public errorMessages: Record<string, { type: string; message: string }[]> = {
    email: [
      { type: 'required', message: 'L\'email est obligatoire' },
      { type: 'pattern', message: 'L\'email n\'est pas valide' }
    ],
    password: [
      { type: 'required', message: 'Le mot de passe est obligatoire' },
      { type: 'minlength', message: 'Le mot de passe doit contenir au moins 8 caractères' },
      { type: 'strongPassword', message: 'Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial' }
    ]
  };

  public fb = inject(FormBuilder);
  public navigationService = inject(NavigationService);
  public authService = inject(AuthenticationService);
  public notificationService = inject(NotificationService);
  public modalService = inject(BsModalService);

  constructor(
  ) { }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.pattern(this.emailRegex)]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        this.strongPasswordValidator()
      ]],
      rememberMe: [false]
    });
  }

  public strongPasswordValidator(): unknown {
    return (control: AbstractControl): { [key: string]: unknown } | null => {
      if (!control.value) {
        return null;
      }
      const hasUpperCase = /[A-Z]/.test(control.value);
      const hasLowerCase = /[a-z]/.test(control.value);
      const hasNumber = /[0-9]/.test(control.value);
      const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(control.value);

      const valid = hasUpperCase && hasLowerCase && hasNumber && hasSpecialChar;

      return !valid ? { 'strongPassword': true } : null;
    };
  }

  public getErrorMessage(controlName: string): string {
    const control = this.loginForm?.get(controlName);
    if (control && control.errors && this.submitted) {
      const errors = this.errorMessages[controlName];
      for (const error of errors) {
        if (control.errors[error.type]) {
          return error.message;
        }
      }
    }
    return '';
  }

  public isFormValid(): boolean | undefined {
    return this.loginForm?.valid;
  }

  @HostListener('document:keydown.enter')
  public onEnterKey(): void {
    if (this.isFormValid() && !this.isLoading) {
      this.login();
    }
  }

  public login(): void {
    this.submitted = true;
    if (this.loginForm?.invalid) {
      return;
    }

    this.isLoading = true;

    const loginData = this.loginForm?.value;
    this.authService.login(loginData).subscribe({
      next: (res) => {
        this.authService.localLogin(
          res.user!,
          res.accessToken!,
          res.tokenExpiredAt!,
        );
        this.navigationService.goTo('/dashboard');
        this.isLoading = false
        this.notificationService.success('Vous êtes connecté(e) avec succès');
      },
      error: () => {
        this.notificationService.danger(
          'Login ou mot de passe incorrect ou vérifiez votre connexion à internet'
        );
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      },
    });
  }
  public toggleViewPasswordBtn(): void {
    this.passwordType = this.passwordType === 'password' ? 'text' : 'password';
  }
}


