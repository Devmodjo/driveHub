import { Component, inject, Input, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth-service/auth.service';
import { RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";
import { UserModel } from '../../../model/user.model';
import { Role } from '../../../enums/role.enum';
import { ModalComponent } from '../modal/modal.component';
import UserRegisterModel from '../../../interfaces/UserRegisterModel';


/**
 * l'attribut providers permet d'indiquer
 * à angular la liste des services à utiliser dans un
 * composant
 */
@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, RouterLinkActive, RouterOutlet, ModalComponent],
  providers: [AuthService], // l'on founir la classe authService au composant
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterComponent {

  // permet d'injecter un service dans  un composant
  private authService = inject(AuthService);
  @Input() isSend = signal<boolean>(false);
  errorMessage = signal<string>(""); // message d'erreur affiché sous le formulaire
  role = Role;

  registerForm = new FormGroup({

    name: new FormControl("", [Validators.required]),
    email: new FormControl("", [Validators.required, Validators.email]),
    role: new FormControl("", [Validators.required]),
    password: new FormControl("", [Validators.required]),
    residence : new FormControl("", [Validators.required]),
    phoneNumber : new FormControl("", [
      Validators.required, 
      Validators.pattern("^[0-9]+$")
    ])
    
  });

  onSubmit() {

    if (this.registerForm.invalid) {
      this.isSend.set(false);
      return;
    }
    
    /**
     * on construit l'objet qui vas communique avec l'api
     */
    const registerCredential : UserRegisterModel = {
      name: this.registerForm.value.name!,
      email: this.registerForm.value.email!,
      role: this.registerForm.value.role! as Role,
      password: this.registerForm.value.password!,
      residence: this.registerForm.value.residence!,
      phoneNumber: this.registerForm.value.phoneNumber!
    }
  
  /**
   * grace à l'observable, on verifie si l'api repond
   * et on s'abonne au resultat via subscribe 
   * celle ci possede deux paramètre next et error
   * next : si l'api repond avec succes
   * error : si l'api repond avec une erreur
   */
    this.authService.register(registerCredential).subscribe({
      next : (response) => {
        this.errorMessage.set(""); // effacer les erreurs précédentes si succès
        this.isSend.set(true);
      },
      error : (error) => {
        this.isSend.set(false);

        // On choisit le message selon le code HTTP retourné par l'API
        if (error.status === 409) {
          // 409 = Conflit → email déjà utilisé
          this.errorMessage.set("Cette adresse e-mail est déjà enregistrée.");
        } else if (error.status === 400) {
          // 400 = données invalides côté serveur
          this.errorMessage.set("Les informations saisies sont incorrectes. Vérifiez le formulaire.");
        } else if (error.status === 0) {
          // 0 = serveur injoignable
          this.errorMessage.set("Serveur injoignable. Réessayez plus tard.");
        } else {
          // Cas générique : message du backend s'il existe
          this.errorMessage.set(error.error?.message || "Une erreur est survenue. Réessayez.");
        }
      }
    })
      
  }

  handleModal() { return this.isSend();  }

}
