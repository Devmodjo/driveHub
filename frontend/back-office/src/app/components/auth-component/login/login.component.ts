import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";
import { AuthService } from '../../../services/auth-service/auth.service';
import { Router } from '@angular/router';
import UserLoginCredentials from '../../../interfaces/UserLoginCredentials';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, RouterLinkActive, RouterOutlet],
  providers: [AuthService],
  templateUrl: './login.html',
  styleUrl: './login.css',
}) 
export class LoginComponent {

  private authService = inject(AuthService);
  private router = inject(Router);
  errorMessage = signal<string>("");
  isLoading = signal<boolean>(false);

  loginForm = new FormGroup({
    email: new FormControl("", [Validators.required, Validators.email]),
    password: new FormControl("", [Validators.required])
  })

  onSubmit(){

    if (this.loginForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set("");


    // on construit l'objet à envoyer à l'api
    const credentials : UserLoginCredentials = {
      email: this.loginForm.value.email!,
      password: this.loginForm.value.password!
    }

    // On appelle le service et on s'ABONNE au résultat
    // C'est ici que la requête HTTP part réellement
    this.authService.login(credentials).subscribe({
      next: (response) => {
        // 1. Stocker le token
        this.authService.saveToken(response.token);
        
        // 2. Rediriger vers le dashboard
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        // 3. Gérer l'erreur
        this.isLoading.set(false);
        this.errorMessage.set(err.error.message || "Une erreur est survenue");
      }
    })
  }

}
