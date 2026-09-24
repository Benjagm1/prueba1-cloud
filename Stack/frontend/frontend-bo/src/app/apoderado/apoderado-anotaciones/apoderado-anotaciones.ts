import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApoderadoApiService, PupiloResumen } from '../../core/services/apoderado-api.service';
import { AnotacionDto } from '../../core/services/docente-api.service';
import { nombreCompletoPupilo } from '../../core/utils/pupilo.util';

@Component({
  selector: 'app-apoderado-anotaciones',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './apoderado-anotaciones.html',
  styleUrl: './apoderado-anotaciones.css',
})
export class ApoderadoAnotaciones implements OnInit {
  private readonly api = inject(ApoderadoApiService);

  readonly pupilos = signal<PupiloResumen[]>([]);
  readonly anotaciones = signal<AnotacionDto[]>([]);
  readonly cargando = signal(true);
  readonly error = signal('');

  pupiloId: number | null = null;

  ngOnInit(): void {
    void this.cargarPupilos();
  }

  etiquetaPupilo(p: PupiloResumen): string {
    return nombreCompletoPupilo(p);
  }

  etiquetaTipo(tipo: string): string {
    return tipo === 'negativa' ? 'Observación' : 'Reconocimiento';
  }

  async cargarPupilos(): Promise<void> {
    this.cargando.set(true);
    try {
      const lista = await firstValueFrom(this.api.misPupilos());
      this.pupilos.set(lista ?? []);
      if (lista.length === 1) {
        this.pupiloId = lista[0].alumnoUsuarioId;
        await this.cargarAnotaciones();
      }
    } catch {
      this.error.set('No se pudieron cargar sus pupilos.');
    } finally {
      this.cargando.set(false);
    }
  }

  async onPupiloChange(): Promise<void> {
    await this.cargarAnotaciones();
  }

  async cargarAnotaciones(): Promise<void> {
    if (this.pupiloId == null) {
      this.anotaciones.set([]);
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    try {
      const lista = await firstValueFrom(this.api.anotacionesPupilo(this.pupiloId));
      this.anotaciones.set(lista ?? []);
    } catch {
      this.error.set('No se pudieron cargar las anotaciones del pupilo.');
      this.anotaciones.set([]);
    } finally {
      this.cargando.set(false);
    }
  }
}
