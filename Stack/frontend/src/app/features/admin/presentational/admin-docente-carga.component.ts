import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Asignatura, Curso, Usuario } from '../../../shared/models/catalog.model';
import { etiquetaAsignatura, etiquetaCurso, etiquetaUsuario } from '../../../shared/utils/labels';

export interface AsignacionDocenteResumen {
  cursoId: number;
  cursoNombre: string;
  asignaturaId: number;
  asignaturaNombre: string;
}

@Component({
  selector: 'app-admin-docente-carga',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="form-grid form-grid--2" style="max-width: 640px; margin-bottom: 1.5rem">
      <div class="field">
        <label for="prof">Profesor</label>
        <select id="prof" [(ngModel)]="profesorId" name="prof" (ngModelChange)="profesorChange.emit($event)">
          <option [ngValue]="null">Seleccione un profesor…</option>
          @for (p of profesores; track p.idUsuario) {
            <option [ngValue]="p.idUsuario">{{ etiquetaUsuario(p) }}</option>
          }
        </select>
      </div>
    </div>

    @if (profesorId != null) {
      <div class="form-section">
        <p class="form-section__title">Asignaciones actuales</p>
        @if (asignaciones.length === 0) {
          <p class="muted">Este profesor aún no tiene cursos asignados.</p>
        } @else {
          <div class="table-wrap">
            <table class="data">
              <thead>
                <tr><th>Curso</th><th>Asignatura</th></tr>
              </thead>
              <tbody>
                @for (a of asignaciones; track a.cursoId + '-' + a.asignaturaId) {
                  <tr>
                    <td>{{ a.cursoNombre }}</td>
                    <td>{{ a.asignaturaNombre }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>

      <div class="form-section">
        <p class="form-section__title">Agregar curso y asignatura</p>
        <p class="muted" style="margin: 0 0 1rem">
          Un mismo profesor puede impartir la misma asignatura en varios cursos (ej. Matemáticas en 5°, 6°, 7° y 8°).
        </p>
        <div class="form-grid form-grid--2" style="max-width: 640px">
          <div class="field">
            <label for="nc">Curso</label>
            <select id="nc" [(ngModel)]="nuevoCursoId" name="nc">
              <option [ngValue]="null">Seleccione…</option>
              @for (c of cursos; track c.id) {
                <option [ngValue]="c.id">{{ cursoLabel(c) }}</option>
              }
            </select>
          </div>
          <div class="field">
            <label for="na">Asignatura</label>
            <select id="na" [(ngModel)]="nuevaAsignaturaId" name="na">
              <option [ngValue]="null">Seleccione…</option>
              @for (a of asignaturas; track a.id) {
                <option [ngValue]="a.id">{{ asignaturaLabel(a) }}</option>
              }
            </select>
          </div>
        </div>
        <div class="form-actions" style="border: none; padding-top: 0.5rem">
          <button type="button" class="btn btn-primary" [disabled]="saving || nuevoCursoId == null || nuevaAsignaturaId == null" (click)="agregar()">
            Agregar asignación
          </button>
        </div>
      </div>
    }
  `,
})
export class AdminDocenteCargaComponent {
  @Input() profesores: Usuario[] = [];
  @Input() cursos: Curso[] = [];
  @Input() asignaturas: Asignatura[] = [];
  @Input() asignaciones: AsignacionDocenteResumen[] = [];
  @Input() saving = false;

  @Output() readonly profesorChange = new EventEmitter<number | null>();
  @Output() readonly agregarAsignacion = new EventEmitter<{ cursoId: number; asignaturaId: number }>();

  profesorId: number | null = null;
  nuevoCursoId: number | null = null;
  nuevaAsignaturaId: number | null = null;

  readonly cursoLabel = etiquetaCurso;
  readonly asignaturaLabel = etiquetaAsignatura;
  readonly etiquetaUsuario = etiquetaUsuario;

  agregar(): void {
    if (this.nuevoCursoId == null || this.nuevaAsignaturaId == null) return;
    this.agregarAsignacion.emit({ cursoId: this.nuevoCursoId, asignaturaId: this.nuevaAsignaturaId });
    this.nuevoCursoId = null;
    this.nuevaAsignaturaId = null;
  }
}
