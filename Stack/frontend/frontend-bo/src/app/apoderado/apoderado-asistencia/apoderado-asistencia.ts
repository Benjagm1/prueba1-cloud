import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApoderadoApiService, PupiloResumen } from '../../core/services/apoderado-api.service';
import { RegistroAsistenciaDto } from '../../core/services/alumno-api.service';
import { nombreCompletoPupilo, rangoMesActual } from '../../core/utils/pupilo.util';

@Component({
  selector: 'app-apoderado-asistencia',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './apoderado-asistencia.html',
  styleUrl: './apoderado-asistencia.css',
})
export class ApoderadoAsistencia implements OnInit {
  private readonly api = inject(ApoderadoApiService);

  readonly pupilos = signal<PupiloResumen[]>([]);
  readonly registros = signal<RegistroAsistenciaDto[]>([]);
  readonly cargando = signal(true);
  readonly error = signal('');

  pupiloId: number | null = null;

  ngOnInit(): void {
    void this.cargarPupilos();
  }

  etiquetaPupilo(p: PupiloResumen): string {
    return nombreCompletoPupilo(p);
  }

  async cargarPupilos(): Promise<void> {
    this.cargando.set(true);
    try {
      const lista = await firstValueFrom(this.api.misPupilos());
      this.pupilos.set(lista ?? []);
      if (lista.length === 1) {
        this.pupiloId = lista[0].alumnoUsuarioId;
        await this.cargarAsistencia();
      }
    } catch {
      this.error.set('No se pudieron cargar sus pupilos.');
    } finally {
      this.cargando.set(false);
    }
  }

  async onPupiloChange(): Promise<void> {
    await this.cargarAsistencia();
  }

  async cargarAsistencia(): Promise<void> {
    if (this.pupiloId == null) {
      this.registros.set([]);
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    const { desde, hasta } = rangoMesActual();
    try {
      const historial = await firstValueFrom(this.api.historialPupilo(this.pupiloId, desde, hasta));
      this.registros.set(historial ?? []);
    } catch {
      this.error.set('No se pudo cargar la asistencia del pupilo.');
      this.registros.set([]);
    } finally {
      this.cargando.set(false);
    }
  }
}
