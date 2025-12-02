import { inject, Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpHeaders } from '@angular/common/http';

import { Observable } from 'rxjs';
import { AuthenticationService } from '../services/authentication.service';
import { NotificationService } from '../services/notification.service';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {

  private excludedRoutes = ['/api/v1/auth/authenticate', '/users/create'];

  public authService = inject(AuthenticationService);
  public notifService = inject(NotificationService);

  constructor() { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // const user = this.authService.getUser();
    // if (this.authService.getUser() && user && user.status !== STATE.STATE_ACTIVATED) {
    //   this.authService.logout();
    //   this.notifService.info("Votre compte utilisateur est désactivé, veuillez contacter un administrateur pour plus de détails");
    // }

    if (this.authService.getAccessToken() && this.shouldAddToken(request.url)) {
      request = this.addToken(request, this.authService.getAccessToken() ?? '');
    }
    return next.handle(request);
  }

  private shouldAddToken(url: string): boolean {
    return !this.excludedRoutes.some(route => url.endsWith(route));
  }

  private addToken(request: HttpRequest<unknown>, token: string) {
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    return request.clone({ headers });
  }
}
