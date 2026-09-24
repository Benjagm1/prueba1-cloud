import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  let headers = req.headers;
  if (!req.url.startsWith('/auth/login')) {
    try {
      const raw = localStorage.getItem('libro_clases_session');
      if (raw) {
        const session = JSON.parse(raw) as { token?: string };
        if (session.token) {
          headers = headers.set('Authorization', `Bearer ${session.token}`);
        }
      }
    } catch {
      /* ignore */
    }
  }
  return next(req.clone({ headers })).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.startsWith('/auth/login')) {
        localStorage.removeItem('libro_clases_session');
        void router.navigate(['/login/form']);
      }
      return throwError(() => err);
    }),
  );
};
