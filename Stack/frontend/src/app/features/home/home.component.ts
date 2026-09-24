import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PageHeaderComponent } from '../../shared/components/page-header.component';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, PageHeaderComponent],
  template: `
    <app-page-header
      title="Bienvenido al libro de clases"
      subtitle="Consulte notas, asistencia y mensajes según su perfil en el colegio."
    />

    <div class="tile-grid">
      @if (auth.isAdmin()) {
        <a routerLink="/admin" class="tile">
          <div class="tile__icon" aria-hidden="true">⚙</div>
          <h3>Administración</h3>
          <p>Crear cuentas, asignar cursos, apoderados y carga docente.</p>
        </a>
      }
      @if (auth.isProfesor()) {
        <a routerLink="/profesor" class="tile">
          <div class="tile__icon" aria-hidden="true">📋</div>
          <h3>Panel docente</h3>
          <p>Lista de alumnos, registro de notas y asistencia.</p>
        </a>
      }
      @if (auth.isApoderado()) {
        <a routerLink="/apoderado" class="tile">
          <div class="tile__icon" aria-hidden="true">👨‍👩‍👧</div>
          <h3>Seguimiento del pupilo</h3>
          <p>Notas, asistencia y datos personales de su hijo o hija.</p>
        </a>
      }
      @if (auth.isAlumno()) {
        <a routerLink="/alumno" class="tile">
          <div class="tile__icon" aria-hidden="true">📚</div>
          <h3>Mis resultados</h3>
          <p>Revise sus calificaciones y resumen de asistencia.</p>
        </a>
      }
      <a routerLink="/mensajes" class="tile">
        <div class="tile__icon" aria-hidden="true">✉</div>
        <h3>Mensajes</h3>
        <p>Bandeja de mensajes y notificaciones institucionales.</p>
      </a>
      <a routerLink="/perfil" class="tile">
        <div class="tile__icon" aria-hidden="true">👤</div>
        <h3>Mi perfil</h3>
        <p>Actualice su correo y teléfono de contacto.</p>
      </a>
    </div>
  `,
})
export class HomeComponent {
  readonly auth = inject(AuthService);
}
