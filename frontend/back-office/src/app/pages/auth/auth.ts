import { Component } from '@angular/core';
import { Register } from "../../components/auth-component/register/register";

@Component({
  selector: 'app-auth',
  imports: [Register],
  templateUrl: './auth.html',
  styleUrl: './auth.css',
})
export class Auth {

}
