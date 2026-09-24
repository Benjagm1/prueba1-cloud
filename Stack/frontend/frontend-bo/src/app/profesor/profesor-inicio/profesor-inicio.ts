import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { DocenteApiService, NotificacionDto } from '../../core/services/docente-api.service';
import {
  ClaseHorario,
  esDiaLectivo,
  horarioDelDia,
  proximaClaseHoy,
  badgeCssEstado,
  claseCssEstado,
  etiquetaEstado,
} from '../../core/utils/horario.util';

@Component({
  selector: 'app-profesor-inicio',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe],
  templateUrl: './profesor-inicio.html',
  styleUrl: './profesor-inicio.css',
})
export class ProfesorInicio implements OnInit {
  private readonly api = inject(DocenteApiService);

  readonly cargando = signal(true);
  readonly clasesHoy = signal<ClaseHorario[]>([]);
  readonly esHoyLectivo = signal(true);
  readonly mensajesNuevos = signal(0);
  readonly asistenciaPct = signal<string>('—');
  readonly asistenciaSub = signal('SEMANA ACTUAL');
  readonly recordatorios = signal<NotificacionDto[]>([]);
  readonly clasesHoyLabel = signal('0 Bloques');
  readonly proximaClaseLabel = signal('Sin clases hoy');

  readonly badgeCssEstado = badgeCssEstado;
  readonly claseCssEstado = claseCssEstado;
  readonly etiquetaEstado = etiquetaEstado;

  ngOnInit(): void {
    void this.cargarDashboard();
  }

  private inicioSemana(fecha: Date): string {
    const d = new Date(fecha);
    const dia = d.getDay();
    const diff = dia === 0 ? -6 : 1 - dia;
    d.setDate(d.getDate() + diff);
    return d.toISOString().slice(0, 10);
  }

  private finSemana(fecha: Date): string {
    const d = new Date(fecha);
    const dia = d.getDay();
    const diff = dia === 0 ? 0 : 7 - dia;
    d.setDate(d.getDate() + diff);
    return d.toISOString().slice(0, 10);
  }

  async cargarDashboard(): Promise<void> {
    this.cargando.set(true);
    const ahora = new Date();
    this.esHoyLectivo.set(esDiaLectivo(ahora));

    try {
      const asignaciones = await firstValueFrom(this.api.misAsignaciones());
      const clases = horarioDelDia(asignaciones, ahora);
      this.clasesHoy.set(clases);
      this.clasesHoyLabel.set(`${clases.length} Bloque${clases.length === 1 ? '' : 's'}`);

      if (!this.esHoyLectivo()) {
        this.proximaClaseLabel.set('No hay clases (fin de semana)');
      } else {
        const prox = proximaClaseHoy(clases);
        if (prox) {
          this.proximaClaseLabel.set(`PRÓXIMO: ${prox.cursoNombre} (${prox.inicio})`);
        } else if (clases.length > 0) {
          this.proximaClaseLabel.set('Todas las clases completadas');
        } else {
          this.proximaClaseLabel.set('Sin bloques asignados');
        }
      }
    } catch {
      this.clasesHoy.set([]);
      this.clasesHoyLabel.set('—');
      this.proximaClaseLabel.set('No disponible');
    }

    try {
      const mensajes = await firstValueFrom(this.api.mensajesRecibidos());
      this.mensajesNuevos.set(mensajes.filter((m) => !m.leido).length);
    } catch {
      this.mensajesNuevos.set(0);
    }

    try {
      const desde = this.inicioSemana(ahora);
      const hasta = this.finSemana(ahora);
      const resumen = await firstValueFrom(this.api.resumenAsistenciaDocente(desde, hasta));
      if (resumen.diasRegistrados === 0) {
        this.asistenciaPct.set('—');
        this.asistenciaSub.set('SIN REGISTROS ESTA SEMANA');
      } else {
        this.asistenciaPct.set(`${Math.round(resumen.porcentaje)}%`);
        this.asistenciaSub.set('SEMANA ACTUAL');
      }
    } catch {
      this.asistenciaPct.set('—');
      this.asistenciaSub.set('NO DISPONIBLE');
    }

    try {
      const notifs = await firstValueFrom(this.api.misNotificaciones());
      this.recordatorios.set(notifs.slice(0, 5));
    } catch {
      this.recordatorios.set([]);
    }

    this.cargando.set(false);
  }
}
