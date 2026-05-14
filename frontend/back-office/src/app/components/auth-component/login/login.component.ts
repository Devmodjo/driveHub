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
        this.isLoading.set(false);

        // On choisit le message selon le code HTTP retourné par l'API
        if (err.status === 401) {
          // 401 = identifiants incorrects
          this.errorMessage.set("Email ou mot de passe incorrect.");
        } else if (err.status === 403) {
          // 403 = compte en attente de validation admin
          this.errorMessage.set("Votre compte est en attente de validation par un administrateur.");
        } else if (err.status === 404) {
          // 404 = aucun compte trouvé avec cet email
          this.errorMessage.set("Aucun compte associé à cet email.");
        } else if (err.status === 0) {
          // 0 = serveur injoignable (pas de connexion ou backend arrêté)
          this.errorMessage.set("Impossible de contacter le serveur. Vérifiez votre connexion.");
        } else {
          // Cas générique : on affiche le message du backend s'il existe
          this.errorMessage.set(err.error?.message || "Une erreur est survenue. Réessayez.");
        }
      }
    })
  }

}
