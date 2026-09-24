import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { DocenteApiService } from '../../core/services/docente-api.service';
import { CursoCard, mapAsignaciones } from '../../core/utils/asignacion.util';

@Component({
  selector: 'app-calificaciones-cursos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './calificaciones-cursos.html',
  styleUrl: './calificaciones-cursos.css',
})
export class CalificacionesCursos implements OnInit {
  private readonly api = inject(DocenteApiService);
  private readonly router = inject(Router);

  readonly cursosCalificaciones = signal<CursoCard[]>([]);
  readonly cargando = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    this.api
      .misAsignaciones()
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (data) => {
          this.cursosCalificaciones.set(mapAsignaciones(data));
          this.enriquecerConteoAlumnos();
        },
        error: () => {
          this.error.set('No se pudieron cargar sus cursos.');
        },
      });
  }

  private enriquecerConteoAlumnos(): void {
    for (const curso of this.cursosCalificaciones()) {
      this.api.listaAlumnos(curso.cursoId, curso.asignaturaId).subscribe({
        next: (alumnos) => {
          this.cursosCalificaciones.update((lista) =>
            lista.map((c) =>
              c.cursoId === curso.cursoId && c.asignaturaId === curso.asignaturaId
                ? { ...c, alumnos: String(alumnos.length) }
                : c,
            ),
          );
        },
        error: () => {
          this.cursosCalificaciones.update((lista) =>
            lista.map((c) =>
              c.cursoId === curso.cursoId && c.asignaturaId === curso.asignaturaId
                ? { ...c, alumnos: '—' }
                : c,
            ),
          );
        },
      });
    }
  }

  abrirRegistro(badge: string): void {
    void this.router.navigate(['/profesor/calificaciones/registro', badge]);
  }
}
