import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApoderadoApiService, PupiloResumen } from '../../core/services/apoderado-api.service';
import { CalificacionDto } from '../../core/services/docente-api.service';
import { nombreCompletoPupilo } from '../../core/utils/pupilo.util';

interface FilaNota {
  asignatura: string;
  evaluacion: string;
  nota: number;
}

@Component({
  selector: 'app-apoderado-calificaciones',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './apoderado-calificaciones.html',
  styleUrl: './apoderado-calificaciones.css',
})
export class ApoderadoCalificaciones implements OnInit {
  private readonly api = inject(ApoderadoApiService);

  readonly pupilos = signal<PupiloResumen[]>([]);
  readonly filas = signal<FilaNota[]>([]);
  readonly curso = signal('');
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
        await this.cargarNotas();
      }
    } catch {
      this.error.set('No se pudieron cargar sus pupilos.');
    } finally {
      this.cargando.set(false);
    }
  }

  async onPupiloChange(): Promise<void> {
    await this.cargarNotas();
  }

  async cargarNotas(): Promise<void> {
    if (this.pupiloId == null) {
      this.filas.set([]);
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    try {
      const [ficha, notas] = await Promise.all([
        firstValueFrom(this.api.fichaPupilo(this.pupiloId)),
        firstValueFrom(this.api.notasPupilo(this.pupiloId)),
      ]);
      this.curso.set(ficha.cursoNombre);
      const mapAsig = new Map(ficha.asignaturas.map((a) => [a.id, a.nombre]));
      this.filas.set(
        (notas ?? [])
          .map((n: CalificacionDto) => ({
            asignatura: mapAsig.get(n.asignaturaId) ?? `Asignatura ${n.asignaturaId}`,
            evaluacion: n.nombreEvaluacion,
            nota: Number(n.nota),
          }))
          .sort((a, b) => a.asignatura.localeCompare(b.asignatura, 'es') || a.evaluacion.localeCompare(b.evaluacion, 'es')),
      );
    } catch {
      this.error.set('No se pudieron cargar las calificaciones.');
      this.filas.set([]);
    } finally {
      this.cargando.set(false);
    }
  }
}
