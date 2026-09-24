import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { DocenteApiService } from '../../core/services/docente-api.service';
import { CursoCard, mapAsignaciones } from '../../core/utils/asignacion.util';

@Component({
  selector: 'app-asistencia-cursos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './asistencia-cursos.html',
  styleUrl: './asistencia-cursos.css',
})
export class AsistenciaCursos implements OnInit {
  private readonly api = inject(DocenteApiService);
  private readonly router = inject(Router);

  readonly cursosAsistencia = signal<CursoCard[]>([]);
  readonly cargando = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    this.api
      .misAsignaciones()
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (data) => {
          this.cursosAsistencia.set(mapAsignaciones(data));
          this.enriquecerConteoAlumnos();
        },
        error: () => {
          this.error.set(
            'No se pudieron cargar sus cursos. Verifique que su cuenta tenga permisos de docente.',
          );
        },
      });
  }

  private enriquecerConteoAlumnos(): void {
    for (const curso of this.cursosAsistencia()) {
      this.api.listaAlumnos(curso.cursoId, curso.asignaturaId).subscribe({
        next: (alumnos) => {
          this.cursosAsistencia.update((lista) =>
            lista.map((c) =>
              c.cursoId === curso.cursoId && c.asignaturaId === curso.asignaturaId
                ? { ...c, alumnos: String(alumnos.length) }
                : c,
            ),
          );
        },
        error: () => {
          this.cursosAsistencia.update((lista) =>
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

  abrirRegistro(idCurso: string): void {
    void this.router.navigate(['/profesor/asistencia-registro', idCurso]);
  }
}
