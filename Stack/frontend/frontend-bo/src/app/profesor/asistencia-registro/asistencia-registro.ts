import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { nombreCompletoAlumno, sortAlumnosPorApellidoNombre } from '../../core/utils/alumno.util';

interface AlumnoFila {
  alumnoUsuarioId: number;
  nombre: string;
  apellido: string;
}

interface FilaUi {
  alumnoUsuarioId: number;
  nombre: string;
  estado: 'presente' | 'ausente';
}

@Component({
  selector: 'app-asistencia-registro',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './asistencia-registro.html',
  styleUrl: './asistencia-registro.css',
})
export class AsistenciaRegistro implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);

  idCurso: string | null = '';
  fechaActual = new Date().toISOString().slice(0, 10);
  readonly alumnos = signal<FilaUi[]>([]);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly mensaje = signal('');
  readonly error = signal('');
  private cursoId = 0;
  private asignaturaId = 0;

  ngOnInit(): void {
    this.idCurso = this.route.snapshot.paramMap.get('idCurso');
    if (!this.idCurso?.includes('-')) {
      this.error.set('Curso no válido.');
      this.cargando.set(false);
      return;
    }
    const [c, a] = this.idCurso.split('-').map(Number);
    this.cursoId = c;
    this.asignaturaId = a;
    void this.cargarDia();
  }

  volver(): void {
    void this.router.navigate(['/profesor/asistencia-cursos']);
  }

  setEstado(alumnoUsuarioId: number, estado: 'presente' | 'ausente'): void {
    this.alumnos.update((filas) =>
      filas.map((a) => (a.alumnoUsuarioId === alumnoUsuarioId ? { ...a, estado } : a)),
    );
  }

  async cargarDia(): Promise<void> {
    this.cargando.set(true);
    this.error.set('');
    try {
      const lista = await firstValueFrom(
        this.http.get<AlumnoFila[]>(
          `/api/docentes/lista-alumnos?cursoId=${this.cursoId}&asignaturaId=${this.asignaturaId}`,
        ),
      );
      const mapa = new Map<number, boolean>();
      try {
        const regs = await firstValueFrom(
          this.http.get<{ alumnoUsuarioId: number; presente: boolean }[]>(
            `/api/asistencia/dia?fecha=${this.fechaActual}&cursoId=${this.cursoId}&asignaturaId=${this.asignaturaId}`,
          ),
        );
        for (const r of regs ?? []) {
          mapa.set(r.alumnoUsuarioId, r.presente);
        }
      } catch {
        /* sin registros previos */
      }
      this.alumnos.set(
        sortAlumnosPorApellidoNombre(lista ?? []).map((a) => ({
          alumnoUsuarioId: a.alumnoUsuarioId,
          nombre: nombreCompletoAlumno(a),
          estado: mapa.get(a.alumnoUsuarioId) === false ? 'ausente' : 'presente',
        })),
      );
    } catch {
      this.error.set('No se pudo cargar la lista de alumnos.');
      this.alumnos.set([]);
    } finally {
      this.cargando.set(false);
    }
  }

  onFechaChange(): void {
    void this.cargarDia();
  }

  async guardarAsistencia(): Promise<void> {
    this.guardando.set(true);
    this.mensaje.set('');
    this.error.set('');
    try {
      await firstValueFrom(
        this.http.post('/api/asistencia/dia', {
          fecha: this.fechaActual,
          cursoId: this.cursoId,
          asignaturaId: this.asignaturaId,
          filas: this.alumnos().map((a) => ({
            alumnoUsuarioId: a.alumnoUsuarioId,
            presente: a.estado === 'presente',
          })),
        }),
      );
      this.mensaje.set('Asistencia guardada correctamente.');
    } catch {
      this.error.set('No se pudo guardar la asistencia.');
    } finally {
      this.guardando.set(false);
    }
  }
}
