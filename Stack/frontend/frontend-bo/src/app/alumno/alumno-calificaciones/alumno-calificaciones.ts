import { Component, OnInit, inject, signal } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { AlumnoApiService, AlumnoFicha } from '../../core/services/alumno-api.service';
import { CalificacionDto } from '../../core/services/docente-api.service';
import { firstValueFrom } from 'rxjs';

interface FilaNota {
  asignatura: string;
  evaluacion: string;
  nota: number;
}

@Component({
  selector: 'app-alumno-calificaciones',
  standalone: true,
  templateUrl: './alumno-calificaciones.html',
  styleUrl: './alumno-calificaciones.css',
})
export class AlumnoCalificaciones implements OnInit {
  private readonly api = inject(AlumnoApiService);
  private readonly auth = inject(AuthService);

  readonly filas = signal<FilaNota[]>([]);
  readonly curso = signal('');
  readonly cargando = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    void this.cargar();
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
    try {
      const [ficha, notas] = await Promise.all([
        firstValueFrom(this.api.miFicha()),
        firstValueFrom(this.api.misNotas(userId)),
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
      this.error.set('No se pudieron cargar sus calificaciones.');
      this.filas.set([]);
    } finally {
      this.cargando.set(false);
    }
  }
}
