import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AlumnoApiService, AlumnoFicha } from '../../core/services/alumno-api.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-alumno-inicio',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './alumno-inicio.html',
  styleUrl: './alumno-inicio.css',
})
export class AlumnoInicio implements OnInit {
  private readonly api = inject(AlumnoApiService);
  private readonly auth = inject(AuthService);

  readonly ficha = signal<AlumnoFicha | null>(null);
  readonly asistenciaPct = signal('—');
  readonly asistenciaSub = signal('Sin registros recientes');
  readonly mensajesNuevos = signal(0);
  readonly cargando = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    void this.cargar();
  }

  nombreCompleto(): string {
    const f = this.ficha();
    return f ? `${f.nombre} ${f.apellido}`.trim() : '';
  }

  private rangoSemana(): { desde: string; hasta: string } {
    const hoy = new Date();
    const dia = hoy.getDay();
    const diffLunes = dia === 0 ? -6 : 1 - dia;
    const lunes = new Date(hoy);
    lunes.setDate(hoy.getDate() + diffLunes);
    const domingo = new Date(lunes);
    domingo.setDate(lunes.getDate() + 6);
    const fmt = (d: Date) => d.toISOString().slice(0, 10);
    return { desde: fmt(lunes), hasta: fmt(domingo) };
  }

  async cargar(): Promise<void> {
    this.cargando.set(true);
    this.error.set('');
    const userId = this.auth.user()?.userId;
    try {
      const ficha = await firstValueFrom(this.api.miFicha());
      this.ficha.set(ficha);

      const { desde, hasta } = this.rangoSemana();
      try {
        const resumen = await firstValueFrom(this.api.miResumenAsistencia(desde, hasta));
        if (resumen.diasRegistrados === 0) {
          this.asistenciaPct.set('—');
          this.asistenciaSub.set('Sin registros esta semana');
        } else {
          this.asistenciaPct.set(`${resumen.porcentaje.toFixed(1)}%`);
          this.asistenciaSub.set(`${resumen.diasPresentes}/${resumen.diasRegistrados} días presente`);
        }
      } catch {
        this.asistenciaPct.set('—');
      }

      try {
        const msgs = await firstValueFrom(this.api.mensajesRecibidos());
        this.mensajesNuevos.set(msgs.filter((m) => !m.leido).length);
      } catch {
        this.mensajesNuevos.set(0);
      }
    } catch {
      this.error.set('No se pudo cargar su información escolar.');
      if (userId) {
        /* ficha puede fallar si no está inscrito */
      }
    } finally {
      this.cargando.set(false);
    }
  }
}
