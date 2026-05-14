import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth-service/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  
    const authervice = inject(AuthService);
    const token = authervice.getToken();

    if( !token ) {
        return next(req);
    }

    if(token) {
        req = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        })
    }

    return next(req);
}