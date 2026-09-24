import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Asignatura, Curso, ProvisionFormValue, Usuario } from '../../core/models/admin.model';
import { etiquetaAsignatura, etiquetaCurso, etiquetaUsuario } from '../../core/utils/admin-labels';

@Component({
  selector: 'app-admin-provision-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin-provision-form.html',
  styleUrl: './admin-forms.css',
})
export class AdminProvisionForm {
  @Input() cursos: Curso[] = [];
  @Input() cursosProfesor: Curso[] = [];
  @Input() asignaturas: Asignatura[] = [];
  @Input() apoderados: Usuario[] = [];
  @Input() saving = false;

  cursosParaRol(): Curso[] {
    return this.value.rol === 'profesor' ? (this.cursosProfesor.length ? this.cursosProfesor : this.cursos) : this.cursos;
  }

  @Output() readonly submitForm = new EventEmitter<ProvisionFormValue>();

  value: ProvisionFormValue = this.emptyValue();

  readonly cursoLabel = etiquetaCurso;
  readonly asignaturaLabel = etiquetaAsignatura;
  readonly usuarioLabel = etiquetaUsuario;

  onSubmit(): void {
    if (!this.formValid()) return;
    this.submitForm.emit({ ...this.value });
  }

  resetForm(): void {
    this.value = this.emptyValue();
  }

  markSuccess(): void {
    this.value = this.emptyValue();
  }

  formValid(): boolean {
    const v = this.value;
    if (!v.nombre?.trim() || !v.apellido?.trim() || !v.email?.trim() || !v.contrasena?.trim()) {
      return false;
    }
    if (v.rol === 'alumno') {
      return v.cursoId != null && v.apoderadoUsuarioId != null;
    }
    if (v.rol === 'profesor') {
      return v.cursoId != null && v.asignaturaId != null;
    }
    return false;
  }

  private emptyValue(): ProvisionFormValue {
    return {
      nombre: '',
      apellido: '',
      email: '',
      contrasena: '',
      rol: 'alumno',
      cursoId: null,
      apoderadoUsuarioId: null,
      alumnoUsuarioId: null,
      asignaturaId: null,
    };
  }
}
