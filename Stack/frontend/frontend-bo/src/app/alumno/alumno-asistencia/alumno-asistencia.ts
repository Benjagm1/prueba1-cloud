import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { AlumnoApiService, RegistroAsistenciaDto } from '../../core/services/alumno-api.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-alumno-asistencia',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './alumno-asistencia.html',
  styleUrl: './alumno-asistencia.css',
})
export class AlumnoAsistencia implements OnInit {
  private readonly api = inject(AlumnoApiService);
  private readonly auth = inject(AuthService);

  readonly registros = signal<RegistroAsistenciaDto[]>([]);
  readonly asistenciaPct = signal('—');
  readonly cargando = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    void this.cargar();
  }

  private rangoMes(): { desde: string; hasta: string } {
    const hoy = new Date();
    const desde = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    const hasta = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
    const fmt = (d: Date) => d.toISOString().slice(0, 10);
    return { desde: fmt(desde), hasta: fmt(hasta) };
  }

  async cargar(): Promise<void> {
    const userId = this.auth.user()?.userId;
    if (!userId) {
      this.error.set('Sesión no válida.');
      this.cargando.set(false);
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    const { desde, hasta } = this.rangoMes();
    try {
      const [historial, resumen] = await Promise.all([
        firstValueFrom(this.api.miHistorialAsistencia(userId, desde, hasta)),
        firstValueFrom(this.api.miResumenAsistencia(desde, hasta)),
      ]);
      this.registros.set(historial ?? []);
      if (resumen.diasRegistrados > 0) {
        this.asistenciaPct.set(`${resumen.porcentaje.toFixed(1)}%`);
      }
    } catch {
      this.error.set('No se pudo cargar su asistencia.');
      this.registros.set([]);
    } finally {
      this.cargando.set(false);
    }
  }
}
