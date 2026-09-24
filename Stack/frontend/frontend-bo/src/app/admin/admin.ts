import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../core/services/auth.service';
import { AdminPanel } from './admin-panel/admin-panel';

interface PerfilDto {
  idUsuario: number;
  nombre: string;
  apellido: string;
  email: string;
  tipo: string;
}

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [AdminPanel],
  templateUrl: './admin.html',
  styleUrl: './admin.css',
})
export class Admin implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);

  readonly nombreVisible = signal('Administrador');
  readonly iniciales = signal('AD');

  ngOnInit(): void {
    void this.cargarPerfil();
  }

  async cargarPerfil(): Promise<void> {
    try {
      const p = await firstValueFrom(this.http.get<PerfilDto>('/auth/perfil'));
      const nombre = `${p.nombre} ${p.apellido}`.trim();
      this.nombreVisible.set(nombre || p.email);
      const partes = nombre.split(/\s+/).filter(Boolean);
      const ini =
        partes.length >= 2
          ? `${partes[0][0]}${partes[partes.length - 1][0]}`.toUpperCase()
          : (p.email[0] ?? 'A').toUpperCase();
      this.iniciales.set(ini);
    } catch {
      const email = this.auth.user()?.email ?? 'Administrador';
      this.nombreVisible.set(email);
      this.iniciales.set(email.slice(0, 2).toUpperCase());
    }
  }

  cerrarSesion(event: Event): void {
    event.preventDefault();
    this.auth.logout();
  }
}
