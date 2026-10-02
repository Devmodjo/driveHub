import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth-service/auth.service';
import { SessionService } from '../services/session-service/session.service';
import { API_URL, BASE_URL } from '../utils/UTILS';

/**
 * Ajoute le bon jeton à chaque appel du backend.
 *
 * - /api/platform/...  → jeton de l'administrateur de la plateforme (back-office, AuthService) ;
 * - autres /api/...    → jeton du moniteur / de l'élève (SessionService) + en-tête X-Tenant-ID
 *                        (schéma de son auto-école, lu dans le jeton) attendu par TenantResolutionFilter.
 *
 * Si le backend répond 401 à un utilisateur connecté (jeton expiré ou révoqué), la session est fermée.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.startsWith(BASE_URL)) {
    const adminToken = inject(AuthService).getToken();
    if (adminToken) {
      req = req.clone({ setHeaders: { Authorization: `Bearer ${adminToken}` } });
    }
    return next(req);
  }

  if (!req.url.startsWith(API_URL)) {
    return next(req);
  }

  const session = inject(SessionService);
  const router = inject(Router);
  const token = session.getToken();
  if (!token) {
    return next(req);
  }

  const headers: Record<string, string> = { Authorization: `Bearer ${token}` };
  const tenant = session.tenant();
  if (tenant) {
    headers['X-Tenant-ID'] = tenant;
  }

  return next(req.clone({ setHeaders: headers })).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        session.logout();
        router.navigate(['/connexion']);
      }
      return throwError(() => error);
    }),
  );
};
