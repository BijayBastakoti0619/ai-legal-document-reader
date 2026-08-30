// src/app/core/interceptors/auth.interceptor.ts

import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);

  console.log('REQUEST URL:', req.url);

  // Completely bypass interceptor logic for auth endpoints
  const isPublicAuthRequest =
    req.url.includes('/login') ||
    req.url.includes('/register') ||
    req.url.includes('/refresh');

  if (isPublicAuthRequest) {
    console.log('AUTH REQUEST BYPASSED:', req.url);

    return next(req);
  }

  const token = localStorage.getItem('accessToken');

  let authReq = req;

  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {

      console.log('INTERCEPTOR ERROR:', error.status, req.url);

      // Access token expired
      if (error.status === 401) {

        return authService.refreshToken().pipe(

          switchMap((newTokens) => {

            const retriedReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${newTokens.accessToken}`
              }
            });

            return next(retriedReq);
          }),

          catchError((refreshError) => {

            console.error('REFRESH TOKEN FAILED:', refreshError);

            authService.logout().subscribe();

            return throwError(() => refreshError);
          })
        );
      }

      return throwError(() => error);
    })
  );
};
