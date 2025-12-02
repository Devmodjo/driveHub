import { inject, Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthenticationService } from '../services/authentication.service';
import { NavigationService } from '../services/navigation.service';

/**
 * @author Jauress Ndongmo
 * @email jauressndongmo5287@gmail.com
 */
@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  public authService = inject(AuthenticationService);
  public navigationService = inject(NavigationService);

  constructor() { }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    void route;
    void state;

    if (this.authService.isLoggedIn()) {
      if (this.authService.hasAnyRole(['SUPER_ADMIN'])) {
        return true;
      }
    }

    return false;
  }

}
