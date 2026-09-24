import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { AlumnoApiService } from '../core/services/alumno-api.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-alumno',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './alumno.html',
  styleUrl: './alumno.css',
})
export class Alumno implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly api = inject(AlumnoApiService);
  private readonly router = inject(Router);

  readonly nombreVisible = signal('Alumno');
  readonly iniciales = signal('AL');
  readonly notificacionesNuevas = signal(0);

  ngOnInit(): void {
    void this.cargarPerfil();
    void this.cargarNotificaciones();
  }

  async cargarPerfil(): Promise<void> {
    try {
      const p = await firstValueFrom(this.api.perfil());
      const nombre = `${p.nombre} ${p.apellido}`.trim();
      this.nombreVisible.set(nombre || p.email);
      const partes = nombre.split(/\s+/).filter(Boolean);
      const ini =
        partes.length >= 2
          ? `${partes[0][0]}${partes[partes.length - 1][0]}`.toUpperCase()
          : (p.email[0] ?? 'A').toUpperCase();
      this.iniciales.set(ini);
    } catch {
      const email = this.auth.user()?.email ?? 'Alumno';
      this.nombreVisible.set(email);
      this.iniciales.set(email.slice(0, 2).toUpperCase());
    }
  }

  async cargarNotificaciones(): Promise<void> {
    try {
      const notifs = await firstValueFrom(this.api.misNotificaciones());
      this.notificacionesNuevas.set(notifs.filter((n) => !n.leida).length);
    } catch {
      this.notificacionesNuevas.set(0);
    }
  }

  irMensajes(): void {
    void this.router.navigate(['/alumno/mensajes']);
  }

  cerrarSesion(event: Event): void {
    event.preventDefault();
    this.auth.logout();
  }
}
