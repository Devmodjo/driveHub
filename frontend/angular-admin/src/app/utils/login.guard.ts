import { inject, Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Observable } from 'rxjs/index';
import { AuthenticationService } from '../services/authentication.service';

@Injectable({
  providedIn: 'root'
})
export class LoginGuard implements CanActivate {

  private _authService = inject(AuthenticationService);
  private _router = inject(Router);
  constructor() { }
  canActivate(): Observable<boolean> | Promise<boolean> | boolean {
    if (this._authService.isLoggedIn() && this._authService.hasAnyRole(['SUPER_ADMIN'])) {
      this._router.navigate(['dashboard']);
      return false;
    } else {
      return true;
    }
  }

}
