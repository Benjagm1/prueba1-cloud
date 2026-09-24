import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { DocenteApiService } from '../../core/services/docente-api.service';
import { parseAsignacionKey } from '../../core/utils/asignacion.util';
import { nombreCompletoAlumno, sortAlumnosPorApellidoNombre } from '../../core/utils/alumno.util';

interface AnotacionUi {
  tipo: string;
  texto: string;
  fecha: string;
}

interface AlumnoUi {
  alumnoUsuarioId: number;
  nombre: string;
  inicial: string;
  anotaciones: AnotacionUi[];
}

@Component({
  selector: 'app-anotaciones-registro',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './anotaciones-registro.html',
  styleUrl: './anotaciones-registro.css',
})
export class AnotacionesRegistro implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(DocenteApiService);

  idCurso = '';
  cursoId = 0;
  asignaturaId = 0;
  readonly tituloCurso = signal('');
  readonly alumnos = signal<AlumnoUi[]>([]);
  readonly alumnosFiltrados = signal<AlumnoUi[]>([]);
  readonly alumnoSeleccionado = signal<AlumnoUi | null>(null);
  busqueda = '';
  tipoAnotacion: 'positiva' | 'negativa' = 'positiva';
  textoAnotacion = '';
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal('');

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
          const mapped = sortAlumnosPorApellidoNombre(lista).map((a) => {
            const nombre = nombreCompletoAlumno(a);
            return {
              alumnoUsuarioId: a.alumnoUsuarioId,
              nombre,
              inicial: nombre.charAt(0).toUpperCase(),
              anotaciones: [] as AnotacionUi[],
            };
          });
          this.alumnos.set(mapped);
          this.aplicarFiltro();
        },
        error: () => this.error.set('No se pudo cargar la lista de alumnos.'),
      });
  }

  volver(): void {
    void this.router.navigate(['/profesor/anotaciones']);
  }

  onBusquedaChange(): void {
    this.aplicarFiltro();
  }

  private aplicarFiltro(): void {
    const q = this.busqueda.trim().toLowerCase();
    const fuente = this.alumnos();
    this.alumnosFiltrados.set(
      q ? fuente.filter((a) => a.nombre.toLowerCase().includes(q)) : [...fuente],
    );
  }

  seleccionarAlumno(alumno: AlumnoUi): void {
    this.alumnoSeleccionado.set(alumno);
    this.tipoAnotacion = 'positiva';
    this.textoAnotacion = '';
    this.cargarHistorial(alumno);
  }

  private cargarHistorial(alumno: AlumnoUi): void {
    this.api.anotacionesAlumno(alumno.alumnoUsuarioId).subscribe({
      next: (rows) => {
        const anotaciones = rows.map((r) => ({
          tipo: r.tipo ?? 'positiva',
          texto: r.texto,
          fecha: r.creadoEn,
        }));
        this.actualizarAlumno(alumno.alumnoUsuarioId, { anotaciones });
        const sel = this.alumnoSeleccionado();
        if (sel?.alumnoUsuarioId === alumno.alumnoUsuarioId) {
          this.alumnoSeleccionado.set({ ...sel, anotaciones });
        }
      },
      error: () => {
        this.actualizarAlumno(alumno.alumnoUsuarioId, { anotaciones: [] });
      },
    });
  }

  private actualizarAlumno(alumnoUsuarioId: number, patch: Partial<AlumnoUi>): void {
    this.alumnos.update((lista) =>
      lista.map((a) => (a.alumnoUsuarioId === alumnoUsuarioId ? { ...a, ...patch } : a)),
    );
    this.aplicarFiltro();
  }

  setTipoAnotacion(tipo: 'positiva' | 'negativa'): void {
    this.tipoAnotacion = tipo;
  }

  guardarAnotacion(): void {
    const seleccionado = this.alumnoSeleccionado();
    if (!seleccionado || !this.textoAnotacion.trim()) return;
    this.guardando.set(true);
    this.error.set('');
    this.api
      .crearAnotacion({
        alumnoUsuarioId: seleccionado.alumnoUsuarioId,
        tipo: this.tipoAnotacion,
        texto: this.textoAnotacion.trim(),
        cursoId: this.cursoId,
        asignaturaId: this.asignaturaId,
      })
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe({
        next: () => {
          this.textoAnotacion = '';
          this.cargarHistorial(seleccionado);
        },
        error: () => this.error.set('No se pudo guardar la anotación.'),
      });
  }
}
