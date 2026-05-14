import { Component, inject, Input } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth-service/auth.service';
import { RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";
import { UserModel } from '../../../model/user.model';
import { Role } from '../../../enums/role.enum';
import { ModalComponent } from '../modal/modal.component';


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
  @Input() isSend = false;
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
    
    // throw new Error("le formulaire invalide !!!");
      
  }

  handleModal() { return this.isSend;  }

}
