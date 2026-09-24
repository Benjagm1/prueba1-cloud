import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { fetchAuthSession, getCurrentUser, signIn, signOut } from 'aws-amplify/auth';
import { SessionUser } from '../models/session.model';

const STORAGE_KEY = 'libro_clases_session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly user = signal<SessionUser | null>(this.load());

  constructor(private readonly router: Router) {
    // Sincronizar estado inicial en segundo plano
    void this.syncCurrentUser();
  }

  isLoggedIn(): boolean {
    return !!this.user();
  }

  isProfesor(): boolean {
    return this.user()?.tipo === 'profesor';
  }

  isApoderado(): boolean {
    return this.user()?.tipo === 'apoderado';
  }

  isAlumno(): boolean {
    return this.user()?.tipo === 'alumno';
  }

  isAdmin(): boolean {
    return this.user()?.tipo === 'admin';
  }

  async login(email: string, contrasena: string): Promise<void> {
    // 1. Iniciar sesión directamente contra AWS Cognito
    const signInOutput = await signIn({
      username: email,
      password: contrasena,
    });

    if (!signInOutput.isSignedIn) {
      throw new Error('El usuario requiere pasos adicionales de autenticación.');
    }

    // 2. Obtener sesión activa y tokens
    const session = await fetchAuthSession();
    const idToken = session.tokens?.idToken;
    const tokenStr = idToken?.toString() ?? '';
    const payload = idToken?.payload ?? {};

    // 3. Obtener el tipo/rol (desde custom:tipo, cognito:groups o perfil)
    const groups = (payload['cognito:groups'] as string[]) || [];
    const customTipo = (payload['custom:tipo'] as string) || (payload['profile'] as string);

    let tipoUsuario: SessionUser['tipo'] = 'alumno';
    if (customTipo) {
      tipoUsuario = customTipo as SessionUser['tipo'];
    } else if (groups.includes('admin')) {
      tipoUsuario = 'admin';
    } else if (groups.includes('profesor')) {
      tipoUsuario = 'profesor';
    } else if (groups.includes('apoderado')) {
      tipoUsuario = 'apoderado';
    } else if (groups.includes('alumno')) {
      tipoUsuario = 'alumno';
    }

    const sessionData: SessionUser = {
      token: tokenStr,
      userId: Number(payload['custom:userId']) || 1,
      email: (payload['email'] as string) || email,
      tipo: tipoUsuario,
    };

    localStorage.setItem(STORAGE_KEY, JSON.stringify(sessionData));
    this.user.set(sessionData);
  }

  async logout(): Promise<void> {
    try {
      await signOut();
    } catch {
      /* ignore */
    }
    localStorage.removeItem(STORAGE_KEY);
    this.user.set(null);
    void this.router.navigate(['/login']);
  }

  /** Cierra sesión sin redirigir (p. ej. al elegir perfil en el landing). */
  async logoutSilencioso(): Promise<void> {
    try {
      await signOut();
    } catch {
      /* ignore */
    }
    localStorage.removeItem(STORAGE_KEY);
    this.user.set(null);
  }

  private async syncCurrentUser(): Promise<void> {
    try {
      await getCurrentUser();
      const session = await fetchAuthSession();
      if (!session.tokens?.idToken) {
        this.logoutSilencioso();
      }
    } catch {
      this.logoutSilencioso();
    }
  }

  private load(): SessionUser | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as SessionUser) : null;
    } catch {
      return null;
    }
  }
}