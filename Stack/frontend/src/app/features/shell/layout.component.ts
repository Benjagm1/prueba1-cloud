import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LOGO_URL } from '../../shared/branding';
import { AuthService } from '../../core/services/auth.service';
import { badgeClass, rolLabel } from '../../shared/utils/labels';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-shell">
      <header class="app-header">
        <div class="app-header__inner">
          <a routerLink="/" class="app-brand">
            <img class="app-brand__logo" [src]="logoUrl" alt="" />
            <span class="app-brand__text">
              <span class="app-brand__title">Libro de clases</span>
              <span class="app-brand__sub">Colegio Bernardo O'Higgins</span>
            </span>
          </a>

          <button
            type="button"
            class="nav-toggle"
            [class.is-open]="navAbierto()"
            [attr.aria-expanded]="navAbierto()"
            aria-controls="app-nav"
            (click)="toggleNav()"
          >
            <span class="nav-toggle__bar"></span>
            <span class="nav-toggle__bar"></span>
            <span class="nav-toggle__bar"></span>
            <span class="sr-only">Menú</span>
          </button>

          <nav
            id="app-nav"
            class="app-nav"
            [class.is-open]="navAbierto()"
            aria-label="Principal"
            (click)="cerrarNav()"
          >
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
            <a routerLink="/perfil" routerLinkActive="active">Perfil</a>
            <div class="app-nav__foot">
              <span class="user-chip">
                <span class="badge" [class]="badgeClass(auth.user()?.tipo ?? '')">{{ rolLabel(auth.user()?.tipo ?? '') }}</span>
                <strong>{{ auth.user()?.email }}</strong>
              </span>
              <button type="button" class="btn btn-ghost btn-sm" (click)="auth.logout(); $event.stopPropagation()">
                Salir
              </button>
            </div>
          </nav>
        </div>
      </header>
      <main class="page">
        <router-outlet />
      </main>
    </div>
  `,
})
export class LayoutComponent {
  readonly auth = inject(AuthService);
  readonly logoUrl = LOGO_URL;
  readonly rolLabel = rolLabel;
  readonly badgeClass = badgeClass;
  readonly navAbierto = signal(false);

  toggleNav(): void {
    this.navAbierto.update((v) => !v);
  }

  cerrarNav(): void {
    if (this.navAbierto()) {
      this.navAbierto.set(false);
    }
  }
}
