import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth-service/auth.service';


/**
 * authGuard — protège les routes privées.
 *
 * CanActivateFn est le type Angular moderne pour les guards (Angular 15+).
 * C'est une simple fonction (pas une classe).
 *
 * Elle est appelée automatiquement par Angular AVANT d'afficher la page.
 * Si elle retourne true → la page s'affiche.
 * Si elle retourne false + router.navigate → l'utilisateur est redirigé.
 */
export const authGuardGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if(authService.isLoggedIn()) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};
