import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { DocenteApiService } from '../../core/services/docente-api.service';
import { CursoCard, mapAsignaciones } from '../../core/utils/asignacion.util';

@Component({
  selector: 'app-anotaciones-cursos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './anotaciones-cursos.html',
  styleUrl: './anotaciones-cursos.css',
})
export class AnotacionesCursos implements OnInit {
  private readonly api = inject(DocenteApiService);
  private readonly router = inject(Router);

  readonly cursosAnotaciones = signal<CursoCard[]>([]);
  readonly cargando = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    this.api
      .misAsignaciones()
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (data) => {
          this.cursosAnotaciones.set(mapAsignaciones(data));
          this.enriquecerConteoAlumnos();
        },
        error: () => {
          this.error.set('No se pudieron cargar sus cursos.');
        },
      });
  }

  private enriquecerConteoAlumnos(): void {
    for (const curso of this.cursosAnotaciones()) {
      this.api.listaAlumnos(curso.cursoId, curso.asignaturaId).subscribe({
        next: (alumnos) => {
          this.cursosAnotaciones.update((lista) =>
            lista.map((c) =>
              c.cursoId === curso.cursoId && c.asignaturaId === curso.asignaturaId
                ? { ...c, alumnos: String(alumnos.length) }
                : c,
            ),
          );
        },
        error: () => {
          this.cursosAnotaciones.update((lista) =>
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
    void this.router.navigate(['/profesor/anotaciones/registro', badge]);
  }
}
