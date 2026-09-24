import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthResponse, SessionUser } from '../models/session.model';

const STORAGE_KEY = 'libro_clases_session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly user = signal<SessionUser | null>(this.load());

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

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
    const res = await firstValueFrom(
      this.http.post<AuthResponse>('/auth/login', { email, contrasena }),
    );
    const session: SessionUser = {
      token: res.token,
      userId: res.userId,
      email: res.email,
      tipo: res.tipo,
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    this.user.set(session);
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.user.set(null);
    void this.router.navigate(['/login']);
  }

  /** Cierra sesión sin redirigir (p. ej. al elegir perfil en el landing). */
  logoutSilencioso(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.user.set(null);
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
