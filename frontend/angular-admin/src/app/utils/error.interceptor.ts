import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AuthenticationService } from '../services/authentication.service';
import { NotificationService } from '../services/notification.service';
import { ErrorObject } from '../additional-models/ErrorObject';

@Injectable()

/**
 * @description Cette classe a pour role d'intercepter les erreurs lors des requetes (apres la reponse du serveur)
 * et de les afficher dans la console, c'est un filtre Angular
 * @author Basile Fofack
 * @email juniorbasilefofack@gmail.com
 */
export class ErrorInterceptor implements HttpInterceptor {

  private static showMessage = true;
  private static showForbiddenMessage = true;

  private notifService = inject(NotificationService);
  private authService = inject(AuthenticationService);

  constructor() { }

  public intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    request = request.clone({
      withCredentials: false
    });
    return next.handle(request)
      .pipe(map((event: HttpEvent<unknown>) => {
        if (event instanceof HttpResponse) {
          return event;
        }
        return event;
      }))
      .pipe(catchError((err) => {
        if (err && err.status === 401 && err.error.errorStatus === '401' && err.error.stringErrorCode === 'ACCOUNT_DESACTIVATED') {
          // console.error('Access err: ', err);
          return this.logout();
        } else if (err && err.status === 403 || err.error.errorStatus === '403' || err.error.stringErrorCode === 'TOKEN_EXPIRED') {
          // console.error('Session expired err: ', err);
          return this.forbidden();
        }
        return this.errorToMessage(err);
      }));
  }

  public forbidden(): any[] {
    if (ErrorInterceptor.showForbiddenMessage) {
      this.notifService.warning('Votre session a expirée, veuillez vous connecter à nouveau!');
      ErrorInterceptor.showForbiddenMessage = false;
      setTimeout(() => ErrorInterceptor.showForbiddenMessage = true, 3000);
    }
    this.authService.logout();
    return [];
  }

  public logout(): unknown[] {
    if (ErrorInterceptor.showMessage) {
      this.notifService.warning("Votre compte a été désactivé, veuillez consulter un administateur pour plus de détails!");
    } else {
      ErrorInterceptor.showMessage = true;
    }
    this.authService.logout();
    return [];
  }

  public errorToMessage(err: { error: ErrorObject; }): Observable<unknown> {
    const error: ErrorObject = err.error;
    return throwError(error);
  }

}
