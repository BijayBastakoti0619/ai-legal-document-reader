// src/app/core/interceptors/auth.interceptor.ts

import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Inject the AuthService so we can call refreshToken() and logout()
  const authService = inject(AuthService);
  const token = localStorage.getItem('accessToken');

  // 1. Attach the token if it exists
  let authReq = req;
  if (token) {
    authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
  }

  // 2. Send the request and listen for errors in the RxJS pipeline
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // 3. Catch 401 Unauthorized errors (Make sure we aren't stuck in an infinite loop on the refresh endpoint itself)
      if (error.status === 401 && !req.url.includes('/auth/refresh')) {

        // 4. Pause the failing request and ask the backend for a new set of tokens
        return authService.refreshToken().pipe(
          switchMap((newTokens) => {
            // 5. Success! Clone the original request with the brand new access token
            const retriedReq = req.clone({
              headers: req.headers.set('Authorization', `Bearer ${newTokens.accessToken}`)
            });

            // 6. Send the retried request on its way (the user will never know it failed initially!)
            return next(retriedReq);
          }),
          catchError((refreshError) => {
            // 7. If the refresh token is expired or revoked, boot the user out completely
            authService.logout().subscribe();
            return throwError(() => refreshError);
          })
        );
      }

      // If it is not a 401 error, just pass it along untouched
      return throwError(() => error);
    })
  );
};
