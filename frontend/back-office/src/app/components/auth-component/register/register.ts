import { Component } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {

  registerForm = new FormGroup({

    name: new FormControl("", [Validators.required]),
    email: new FormControl("", [Validators.required]),
    role: new FormControl("", [Validators.required]),
    password: new FormControl("", [Validators.required]),
    residence : new FormControl("", [Validators.required]),
    phoneNumber : new FormControl("", [
      Validators.required, 
      Validators.pattern("^[0-9]+$")
    ])
    
  });

  onSubmit() {
    console.log(this.registerForm.value);
  }
}
