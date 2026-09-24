import { DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { PageHeaderComponent } from '../../shared/components/page-header.component';
import { Calificacion } from '../../shared/models/catalog.model';

interface AsignacionDocente {
  cursoId: number;
  cursoNombre: string;
  asignaturaId: number;
  asignaturaNombre: string;
}

interface AlumnoFila {
  alumnoUsuarioId: number;
  nombre: string;
  apellido: string;
}

interface FilaAsistenciaUi extends AlumnoFila {
  presente: boolean;
}

@Component({
  selector: 'app-profesor-page',
  standalone: true,
  imports: [FormsModule, PageHeaderComponent, DatePipe],
  template: `
    <app-page-header
      title="Libro de clases"
      subtitle="Registre asistencia y calificaciones de sus cursos, como en el libro físico."
    />

    @if (err()) {
      <div class="alert alert-error" role="alert">{{ err() }}</div>
    }
    @if (msg()) {
      <div class="alert alert-ok" role="status">{{ msg() }}</div>
    }

    <section class="card">
      <div class="card__head">
        <h2>Curso y asignatura</h2>
        <p>Seleccione la clase con la que trabajará.</p>
      </div>
      <div class="form-grid" style="max-width: 480px">
        <div class="field">
          <label for="asig">Clase</label>
          <select id="asig" [(ngModel)]="asignacionKey" name="asig" (ngModelChange)="onAsignacionChange()">
            <option value="">Seleccione una clase…</option>
            @for (a of asignaciones(); track asignacionTrack(a)) {
              <option [value]="asignacionTrack(a)">{{ a.cursoNombre }} — {{ a.asignaturaNombre }}</option>
            }
          </select>
        </div>
      </div>
    </section>

    @if (asignacionKey) {
      <nav class="sub-tabs" aria-label="Secciones del libro de clases">
        <button type="button" class="sub-tabs__btn" [class.is-active]="vista() === 'lista'" (click)="vista.set('lista')">
          Lista de alumnos
        </button>
        <button type="button" class="sub-tabs__btn" [class.is-active]="vista() === 'asistencia'" (click)="irAsistencia()">
          Asistencia
        </button>
        <button type="button" class="sub-tabs__btn" [class.is-active]="vista() === 'notas'" (click)="irNotas()">
          Calificaciones
        </button>
      </nav>

      @if (vista() === 'lista') {
        <section class="card">
          <div class="card__head">
            <h2>Alumnos del curso</h2>
            <p>{{ alumnos().length }} estudiantes · {{ asignacionLabel() }}</p>
          </div>
          @if (alumnos().length === 0) {
            <div class="empty-state"><p>No hay alumnos inscritos en este curso.</p></div>
          } @else {
            <div class="table-wrap">
              <table class="data">
                <thead>
                  <tr><th>Apellido</th><th>Nombre</th></tr>
                </thead>
                <tbody>
                  @for (a of alumnos(); track a.alumnoUsuarioId) {
                    <tr>
                      <td>{{ a.apellido }}</td>
                      <td>{{ a.nombre }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </section>
      }

      @if (vista() === 'asistencia') {
        <section class="card">
          <div class="card__head">
            <h2>Registro de asistencia</h2>
            <p>{{ asignacionLabel() }}</p>
          </div>
          <div class="toolbar">
            <div class="field">
              <label for="fechaAsist">Fecha de la clase</label>
              <input id="fechaAsist" type="date" [(ngModel)]="fechaAsistencia" name="fechaAsist" (change)="cargarAsistenciaDia()" />
            </div>
            <button type="button" class="btn btn-ghost btn-sm" (click)="cargarAsistenciaDia()">Cargar / actualizar</button>
          </div>
          @if (filasAsistencia().length > 0) {
            <div class="table-wrap">
              <table class="data">
                <thead>
                  <tr>
                    <th>Apellido</th>
                    <th>Nombre</th>
                    <th>Presente</th>
                  </tr>
                </thead>
                <tbody>
                  @for (f of filasAsistencia(); track f.alumnoUsuarioId) {
                    <tr>
                      <td>{{ f.apellido }}</td>
                      <td>{{ f.nombre }}</td>
                      <td>
                        <label>
                          <input type="checkbox" class="chk-presente" [(ngModel)]="f.presente" [name]="'p' + f.alumnoUsuarioId" />
                          <span [class.presente-si]="f.presente" [class.presente-no]="!f.presente">
                            {{ f.presente ? 'Presente' : 'Ausente' }}
                          </span>
                        </label>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-primary" (click)="guardarAsistencia()" [disabled]="guardando()">
                Guardar asistencia del día
              </button>
            </div>
          }
        </section>
      }

      @if (vista() === 'notas') {
        <section class="card">
          <div class="card__head">
            <h2>Registrar calificación</h2>
            <p>{{ asignacionLabel() }}</p>
          </div>
          <div class="form-grid form-grid--2" style="max-width: 720px">
            <div class="field">
              <label for="alNota">Alumno</label>
              <select id="alNota" [(ngModel)]="alumnoNotaId" name="alNota">
                <option [ngValue]="null">Seleccione…</option>
                @for (a of alumnos(); track a.alumnoUsuarioId) {
                  <option [ngValue]="a.alumnoUsuarioId">{{ a.apellido }}, {{ a.nombre }}</option>
                }
              </select>
            </div>
            <div class="field">
              <label for="ev">Evaluación</label>
              <input id="ev" [(ngModel)]="nombreEvaluacion" name="ev" placeholder="Ej. Control 1, Prueba unidad 2" />
            </div>
            <div class="field">
              <label for="nota">Nota (1,0 – 7,0)</label>
              <input id="nota" type="number" step="0.1" min="1" max="7" [(ngModel)]="notaValor" name="nota" />
            </div>
          </div>
          <div class="form-actions">
            <button type="button" class="btn btn-primary" (click)="guardarNota()" [disabled]="guardando()">
              Registrar nota
            </button>
          </div>
        </section>

        <section class="card">
          <div class="card__head">
            <h2>Notas registradas en este curso</h2>
            <button type="button" class="btn btn-ghost btn-sm" (click)="cargarNotasCurso()">Actualizar</button>
          </div>
          @if (notasCurso().length === 0) {
            <div class="empty-state"><p>Aún no hay notas para mostrar.</p></div>
          } @else {
            <div class="table-wrap">
              <table class="data">
                <thead>
                  <tr><th>Alumno ID</th><th>Evaluación</th><th>Nota</th><th>Fecha</th></tr>
                </thead>
                <tbody>
                  @for (n of notasCurso(); track n.id) {
                    <tr>
                      <td>{{ nombreAlumnoPorId(n.alumnoUsuarioId) }}</td>
                      <td>{{ n.nombreEvaluacion }}</td>
                      <td><strong>{{ n.nota }}</strong></td>
                      <td>{{ n.creadoEn | date: 'dd/MM/yyyy' }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </section>
      }
    }
  `,
})
export class ProfesorPageContainer implements OnInit {
  private readonly http = inject(HttpClient);

  readonly asignaciones = signal<AsignacionDocente[]>([]);
  readonly alumnos = signal<AlumnoFila[]>([]);
  readonly filasAsistencia = signal<FilaAsistenciaUi[]>([]);
  readonly notasCurso = signal<Calificacion[]>([]);
  readonly err = signal('');
  readonly msg = signal('');
  readonly vista = signal<'lista' | 'asistencia' | 'notas'>('lista');
  readonly guardando = signal(false);

  asignacionKey = '';
  fechaAsistencia = new Date().toISOString().slice(0, 10);
  alumnoNotaId: number | null = null;
  nombreEvaluacion = '';
  notaValor: number | null = null;

  private cursoId: number | null = null;
  private asignaturaId: number | null = null;

  ngOnInit(): void {
    void this.loadAsignaciones();
  }

  asignacionTrack(a: AsignacionDocente): string {
    return `${a.cursoId}-${a.asignaturaId}`;
  }

  asignacionLabel(): string {
    const a = this.asignaciones().find((x) => this.asignacionTrack(x) === this.asignacionKey);
    return a ? `${a.cursoNombre} — ${a.asignaturaNombre}` : '';
  }

  nombreAlumnoPorId(id: number): string {
    const a = this.alumnos().find((x) => x.alumnoUsuarioId === id);
    return a ? `${a.apellido}, ${a.nombre}` : `#${id}`;
  }

  onAsignacionChange(): void {
    this.alumnos.set([]);
    this.filasAsistencia.set([]);
    this.notasCurso.set([]);
    this.vista.set('lista');
    if (!this.asignacionKey) {
      this.cursoId = null;
      this.asignaturaId = null;
      return;
    }
    const [c, a] = this.asignacionKey.split('-').map(Number);
    this.cursoId = c;
    this.asignaturaId = a;
    void this.cargarAlumnos();
  }

  async loadAsignaciones(): Promise<void> {
    try {
      const data = await firstValueFrom(this.http.get<AsignacionDocente[]>('/api/docentes/mis-asignaciones'));
      this.asignaciones.set(data);
      if (data.length === 1) {
        this.asignacionKey = this.asignacionTrack(data[0]);
        this.onAsignacionChange();
      }
    } catch {
      this.err.set('No se pudieron cargar sus asignaciones.');
    }
  }

  async cargarAlumnos(): Promise<void> {
    if (this.cursoId == null || this.asignaturaId == null) return;
    try {
      const data = await firstValueFrom(
        this.http.get<AlumnoFila[]>(
          `/api/docentes/lista-alumnos?cursoId=${this.cursoId}&asignaturaId=${this.asignaturaId}`,
        ),
      );
      this.alumnos.set(data);
    } catch {
      this.err.set('No se pudo cargar la lista de alumnos.');
      this.alumnos.set([]);
    }
  }

  async irAsistencia(): Promise<void> {
    this.vista.set('asistencia');
    if (this.alumnos().length === 0) await this.cargarAlumnos();
    await this.cargarAsistenciaDia();
  }

  async irNotas(): Promise<void> {
    this.vista.set('notas');
    if (this.alumnos().length === 0) await this.cargarAlumnos();
    await this.cargarNotasCurso();
  }

  async cargarAsistenciaDia(): Promise<void> {
    if (this.cursoId == null || this.asignaturaId == null) return;
    if (this.alumnos().length === 0) await this.cargarAlumnos();
    const mapa = new Map<number, boolean>();
    try {
      const regs = await firstValueFrom(
        this.http.get<{ alumnoUsuarioId: number; presente: boolean }[]>(
          `/api/asistencia/dia?fecha=${this.fechaAsistencia}&cursoId=${this.cursoId}&asignaturaId=${this.asignaturaId}`,
        ),
      );
      for (const r of regs) {
        mapa.set(r.alumnoUsuarioId, r.presente);
      }
    } catch {
      /* día sin registros previos */
    }
    this.filasAsistencia.set(
      this.alumnos().map((a) => ({
        ...a,
        presente: mapa.get(a.alumnoUsuarioId) ?? true,
      })),
    );
  }

  async guardarAsistencia(): Promise<void> {
    if (this.cursoId == null || this.asignaturaId == null) return;
    this.guardando.set(true);
    this.err.set('');
    this.msg.set('');
    try {
      await firstValueFrom(
        this.http.post('/api/asistencia/dia', {
          fecha: this.fechaAsistencia,
          cursoId: this.cursoId,
          asignaturaId: this.asignaturaId,
          filas: this.filasAsistencia().map((f) => ({
            alumnoUsuarioId: f.alumnoUsuarioId,
            presente: f.presente,
          })),
        }),
      );
      this.msg.set('Asistencia guardada correctamente.');
    } catch (e: unknown) {
      this.err.set(this.parseError(e));
    } finally {
      this.guardando.set(false);
    }
  }

  async guardarNota(): Promise<void> {
    if (this.cursoId == null || this.asignaturaId == null || this.alumnoNotaId == null || !this.nombreEvaluacion.trim()) {
      this.err.set('Complete alumno, nombre de evaluación y nota.');
      return;
    }
    if (this.notaValor == null || this.notaValor < 1 || this.notaValor > 7) {
      this.err.set('La nota debe estar entre 1,0 y 7,0.');
      return;
    }
    this.guardando.set(true);
    this.err.set('');
    this.msg.set('');
    try {
      await firstValueFrom(
        this.http.post('/api/academico/calificaciones', {
          alumnoUsuarioId: this.alumnoNotaId,
          cursoId: this.cursoId,
          asignaturaId: this.asignaturaId,
          nombreEvaluacion: this.nombreEvaluacion.trim(),
          nota: this.notaValor,
        }),
      );
      this.msg.set('Calificación registrada.');
      this.nombreEvaluacion = '';
      this.notaValor = null;
      await this.cargarNotasCurso();
    } catch (e: unknown) {
      this.err.set(this.parseError(e));
    } finally {
      this.guardando.set(false);
    }
  }

  async cargarNotasCurso(): Promise<void> {
    const todas: Calificacion[] = [];
    for (const a of this.alumnos()) {
      try {
        const notas = await firstValueFrom(
          this.http.get<Calificacion[]>(`/api/academico/alumnos/${a.alumnoUsuarioId}/notas`),
        );
        const filtradas = notas.filter(
          (n) => n.cursoId === this.cursoId && n.asignaturaId === this.asignaturaId,
        );
        todas.push(...filtradas);
      } catch {
        /* sin acceso o sin notas */
      }
    }
    todas.sort((x, y) => (y.creadoEn ?? '').localeCompare(x.creadoEn ?? ''));
    this.notasCurso.set(todas);
  }

  private parseError(e: unknown): string {
    if (e instanceof HttpErrorResponse && typeof e.error === 'string') return e.error;
    return 'No se pudo completar la operación.';
  }
}
