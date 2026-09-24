import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isLoggedIn() ? true : router.createUrlTree(['/login/form']);
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAdmin()) return true;
  return router.createUrlTree(['/login/form'], {
    queryParams: { error: 'solo-admin' },
  });
};

export const profesorGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isProfesor()) return true;
  return router.createUrlTree(['/login/form'], {
    queryParams: { perfil: 'profesor', error: 'solo-profesor' },
  });
};

export const apoderadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isApoderado()) return true;
  return router.createUrlTree(['/login/form'], {
    queryParams: { perfil: 'apoderado', error: 'solo-apoderado' },
  });
};

/** Siempre permite ver el formulario de login (no redirige por sesión previa). */
export const loginFormGuard: CanActivateFn = () => true;

export const alumnoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAlumno()) return true;
  return router.createUrlTree(['/login/form'], {
    queryParams: { perfil: 'alumno', error: 'solo-alumno' },
  });
};
