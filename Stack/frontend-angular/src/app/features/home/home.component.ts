import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="card">
      <h2>Bienvenido/a</h2>
      <p>Elige una sección en el menú superior.</p>
      <ul>
        @if (auth.isAdmin()) {
          <li><a routerLink="/admin">Panel administración</a> — crear cuentas y asignaciones.</li>
        }
        @if (auth.isProfesor()) {
          <li><a routerLink="/profesor">Panel profesor</a> — notas, asistencia y mensajes.</li>
        }
        @if (auth.isApoderado()) {
          <li><a routerLink="/apoderado">Panel apoderado</a> — información del pupilo.</li>
        }
        @if (auth.isAlumno()) {
          <li><a routerLink="/alumno">Panel alumno</a> — tus notas y asistencia.</li>
        }
        <li><a routerLink="/mensajes">Mensajes y notificaciones</a></li>
      </ul>
    </div>
  `,
})
export class HomeComponent {
  readonly auth = inject(AuthService);
}
