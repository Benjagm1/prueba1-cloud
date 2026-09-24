import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { fetchAuthSession } from 'aws-amplify/auth';
import { from, catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  // Evitar interceptar la ruta de login
  if (req.url.startsWith('/auth/login') || req.url.includes('/login')) {
    return next(req);
  }

  return from(fetchAuthSession()).pipe(
    switchMap((session) => {
      let headers = req.headers;
      const token = session.tokens?.idToken?.toString();

      if (token) {
        headers = headers.set('Authorization', `Bearer ${token}`);
      }

      return next(req.clone({ headers }));
    }),
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.includes('/login')) {
        void router.navigate(['/login/form']);
      }
      return throwError(() => err);
    })
  );
};