import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { DocenteApiService } from '../../core/services/docente-api.service';
import {
  ClaseHorario,
  NOMBRES_DIA,
  horarioSemanal,
  badgeCssEstado,
  claseCssEstado,
  etiquetaEstado,
} from '../../core/utils/horario.util';

@Component({
  selector: 'app-profesor-horario',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './profesor-horario.html',
  styleUrl: './profesor-horario.css',
})
export class ProfesorHorario implements OnInit {
  private readonly api = inject(DocenteApiService);

  readonly cargando = signal(true);
  readonly error = signal('');
  readonly semana = signal<ClaseHorario[][]>([]);
  readonly hoyIndice = signal(0);

  readonly badgeCssEstado = badgeCssEstado;
  readonly claseCssEstado = claseCssEstado;
  readonly etiquetaEstado = etiquetaEstado;
  readonly nombresDia = NOMBRES_DIA;

  ngOnInit(): void {
    void this.cargar();
  }

  async cargar(): Promise<void> {
    this.cargando.set(true);
    this.error.set('');
    try {
      const asignaciones = await firstValueFrom(this.api.misAsignaciones());
      const ahora = new Date();
      const dia = ahora.getDay();
      this.hoyIndice.set(dia >= 1 && dia <= 5 ? dia - 1 : -1);
      this.semana.set(horarioSemanal(asignaciones, ahora));
    } catch {
      this.error.set('No se pudo cargar el horario.');
      this.semana.set([]);
    } finally {
      this.cargando.set(false);
    }
  }
}
