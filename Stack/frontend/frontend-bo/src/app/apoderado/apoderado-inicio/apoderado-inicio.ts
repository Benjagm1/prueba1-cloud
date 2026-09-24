import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApoderadoApiService, PupiloResumen } from '../../core/services/apoderado-api.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-apoderado-inicio',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './apoderado-inicio.html',
  styleUrl: './apoderado-inicio.css',
})
export class ApoderadoInicio implements OnInit {
  private readonly api = inject(ApoderadoApiService);

  readonly pupilos = signal<PupiloResumen[]>([]);
  readonly notificacionesNuevas = signal(0);
  readonly mensajesNuevos = signal(0);
  readonly cargando = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    void this.cargar();
  }

  nombreCompleto(p: PupiloResumen): string {
    return `${p.nombre} ${p.apellido}`.trim();
  }

  iniciales(p: PupiloResumen): string {
    const n = (p.nombre?.[0] ?? '').toUpperCase();
    const a = (p.apellido?.[0] ?? '').toUpperCase();
    return `${n}${a}` || '?';
  }

  async cargar(): Promise<void> {
    this.cargando.set(true);
    this.error.set('');
    try {
      const [lista, notifs, mensajes] = await Promise.all([
        firstValueFrom(this.api.misPupilos()),
        firstValueFrom(this.api.misNotificaciones()),
        firstValueFrom(this.api.mensajesRecibidos()),
      ]);
      this.pupilos.set(lista ?? []);
      this.notificacionesNuevas.set((notifs ?? []).filter((n) => !n.leida).length);
      this.mensajesNuevos.set((mensajes ?? []).filter((m) => !m.leido).length);
    } catch {
      this.error.set('No se pudo cargar la información de sus pupilos.');
      this.pupilos.set([]);
    } finally {
      this.cargando.set(false);
    }
  }
}
