import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from '../services/session-service/session.service';

/** Espace /dashboard : réservé aux moniteurs et élèves connectés. */
export const sessionGuard: CanActivateFn = (_route, state) => {
  const session = inject(SessionService);
  if (session.isLoggedIn()) {
    return true;
  }
  session.logout();
  return inject(Router).createUrlTree(['/connexion'], { queryParams: { redirect: state.url } });
};

/**
 * Pages qui lisent les données d'une auto-école : l'utilisateur doit en avoir une (tenant dans le jeton).
 * Sinon il est envoyé vers /dashboard/bienvenue (créer son auto-école ou en rejoindre une).
 */
export const tenantGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  return session.tenant() ? true : inject(Router).createUrlTree(['/dashboard/bienvenue']);
};

/** Pages réservées aux moniteurs (gestion de l'auto-école). */
export const monitorGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  return session.isMonitor() ? true : inject(Router).createUrlTree(['/dashboard']);
};
