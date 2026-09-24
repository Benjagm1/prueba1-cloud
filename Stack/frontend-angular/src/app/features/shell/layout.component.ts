import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-shell">
      <header class="top-nav">
        <strong>Libro de clases</strong>
        <nav>
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Inicio</a>
          @if (auth.isAdmin()) {
            <a routerLink="/admin" routerLinkActive="active">Administración</a>
          }
          @if (auth.isProfesor()) {
            <a routerLink="/profesor" routerLinkActive="active">Profesor</a>
          }
          @if (auth.isApoderado()) {
            <a routerLink="/apoderado" routerLinkActive="active">Apoderado</a>
          }
          @if (auth.isAlumno()) {
            <a routerLink="/alumno" routerLinkActive="active">Alumno</a>
          }
          <a routerLink="/mensajes" routerLinkActive="active">Mensajes</a>
          <a routerLink="/perfil" routerLinkActive="active">Mi perfil</a>
          <button type="button" class="btn btn-ghost" (click)="auth.logout()">Salir</button>
        </nav>
      </header>
      <main class="page">
        <p class="muted">Sesión: {{ auth.user()?.email }} · rol: {{ auth.user()?.tipo }}</p>
        <router-outlet />
      </main>
    </div>
  `,
})
export class LayoutComponent {
  readonly auth = inject(AuthService);
}
