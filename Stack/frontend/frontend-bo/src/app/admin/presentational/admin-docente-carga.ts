import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AsignacionDocenteResumen, Asignatura, Curso, Usuario } from '../../core/models/admin.model';
import { etiquetaAsignatura, etiquetaCurso, etiquetaUsuario } from '../../core/utils/admin-labels';

@Component({
  selector: 'app-admin-docente-carga',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin-docente-carga.html',
  styleUrl: './admin-forms.css',
})
export class AdminDocenteCarga {
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
