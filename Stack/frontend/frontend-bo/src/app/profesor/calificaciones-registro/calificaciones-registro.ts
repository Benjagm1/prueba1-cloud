import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { DocenteApiService } from '../../core/services/docente-api.service';
import { parseAsignacionKey } from '../../core/utils/asignacion.util';
import { normalizarNotaChilena } from '../../core/utils/nota.util';
import { nombreCompletoAlumno, sortAlumnosPorApellidoNombre } from '../../core/utils/alumno.util';

const EVALUACIONES = ['Nota 1', 'Nota 2', 'Nota 3'] as const;

interface AlumnoNotas {
  alumnoUsuarioId: number;
  nombre: string;
  n1: number | null;
  n2: number | null;
  n3: number | null;
}

@Component({
  selector: 'app-calificaciones-registro',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './calificaciones-registro.html',
  styleUrl: './calificaciones-registro.css',
})
export class CalificacionesRegistro implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(DocenteApiService);

  idCurso = '';
  cursoId = 0;
  asignaturaId = 0;
  readonly tituloCurso = signal('');
  readonly alumnos = signal<AlumnoNotas[]>([]);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly mensaje = signal('');

  ngOnInit(): void {
    this.idCurso = this.route.snapshot.paramMap.get('idCurso') ?? '';
    const parsed = parseAsignacionKey(this.idCurso);
    if (!parsed) {
      this.error.set('Curso no válido.');
      this.cargando.set(false);
      return;
    }
    this.cursoId = parsed.cursoId;
    this.asignaturaId = parsed.asignaturaId;
    this.cargarDatos();
  }

  private cargarDatos(): void {
    this.api.misAsignaciones().subscribe({
      next: (asignaciones) => {
        const asig = asignaciones.find(
          (a) => a.cursoId === this.cursoId && a.asignaturaId === this.asignaturaId,
        );
        this.tituloCurso.set(asig ? `${asig.cursoNombre} — ${asig.asignaturaNombre}` : this.idCurso);
      },
    });

    this.api
      .listaAlumnos(this.cursoId, this.asignaturaId)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (lista) => {
          const base: AlumnoNotas[] = sortAlumnosPorApellidoNombre(lista).map((a) => ({
            alumnoUsuarioId: a.alumnoUsuarioId,
            nombre: nombreCompletoAlumno(a),
            n1: null,
            n2: null,
            n3: null,
          }));
          if (base.length === 0) {
            this.alumnos.set([]);
            return;
          }
          forkJoin(
            base.map((al) =>
              this.api.notasAlumno(al.alumnoUsuarioId).pipe(catchError(() => of([]))),
            ),
          ).subscribe((notasPorAlumno) => {
            this.alumnos.set(
              base.map((al, idx) => {
                const notas = notasPorAlumno[idx].filter(
                  (n) => n.cursoId === this.cursoId && n.asignaturaId === this.asignaturaId,
                );
                return {
                  ...al,
                  n1: this.buscarNota(notas, EVALUACIONES[0]),
                  n2: this.buscarNota(notas, EVALUACIONES[1]),
                  n3: this.buscarNota(notas, EVALUACIONES[2]),
                };
              }),
            );
          });
        },
        error: () => this.error.set('No se pudo cargar la lista de alumnos.'),
      });
  }

  private buscarNota(notas: { nombreEvaluacion: string; nota: number }[], nombre: string): number | null {
    const hit = notas.filter((n) => n.nombreEvaluacion === nombre).pop();
    return hit ? Number(hit.nota) : null;
  }

  volver(): void {
    this.router.navigate(['/profesor/calificaciones']);
  }

  calcularPromedio(a: AlumnoNotas): string {
    let suma = 0;
    let cant = 0;
    for (const v of [a.n1, a.n2, a.n3]) {
      if (v != null && v !== ('' as unknown)) {
        suma += Number(v);
        cant++;
      }
    }
    return cant === 0 ? '-' : (suma / cant).toFixed(1);
  }

  async guardarNotas(): Promise<void> {
    this.error.set('');
    this.mensaje.set('');
    this.guardando.set(true);
    try {
      for (const al of this.alumnos()) {
        const pares: [typeof EVALUACIONES[number], number | null][] = [
          [EVALUACIONES[0], al.n1],
          [EVALUACIONES[1], al.n2],
          [EVALUACIONES[2], al.n3],
        ];
        for (const [nombreEval, valor] of pares) {
          if (valor === null || valor === ('' as unknown)) continue;
          const nota = normalizarNotaChilena(valor);
          if (nota == null) continue;
          await new Promise<void>((resolve, reject) => {
            this.api
              .registrarNota({
                alumnoUsuarioId: al.alumnoUsuarioId,
                cursoId: this.cursoId,
                asignaturaId: this.asignaturaId,
                nombreEvaluacion: nombreEval,
                nota,
              })
              .subscribe({ next: () => resolve(), error: (e) => reject(e) });
          });
        }
      }
      this.mensaje.set('Notas guardadas correctamente.');
      this.cargarDatos();
    } catch (e: unknown) {
      this.error.set(e instanceof Error ? e.message : 'Error al guardar las notas.');
    } finally {
      this.guardando.set(false);
    }
  }
}
