import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Curso, FamiliaFormValue } from '../../core/models/admin.model';
import { etiquetaCurso } from '../../core/utils/admin-labels';

@Component({
  selector: 'app-admin-familia-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin-familia-form.html',
  styleUrl: './admin-forms.css',
})
export class AdminFamiliaForm {
  @Input() cursos: Curso[] = [];
  @Input() saving = false;
  @Output() readonly submitForm = new EventEmitter<FamiliaFormValue>();

  readonly cursoLabel = etiquetaCurso;

  value: FamiliaFormValue = this.empty();

  onSubmit(): void {
    if (!this.valid()) return;
    this.submitForm.emit({
      ...this.value,
      alumno: { ...this.value.alumno },
      apoderado: { ...this.value.apoderado },
    });
  }

  reset(): void {
    this.value = this.empty();
  }

  markSuccess(): void {
    this.value = this.empty();
  }

  valid(): boolean {
    const { alumno, apoderado, cursoId } = this.value;
    return (
      cursoId != null &&
      !!alumno.nombre?.trim() &&
      !!alumno.apellido?.trim() &&
      !!alumno.email?.trim() &&
      !!alumno.contrasena?.trim() &&
      !!apoderado.nombre?.trim() &&
      !!apoderado.apellido?.trim() &&
      !!apoderado.email?.trim() &&
      !!apoderado.contrasena?.trim()
    );
  }

  private empty(): FamiliaFormValue {
    return {
      cursoId: null,
      alumno: { nombre: '', apellido: '', email: '', contrasena: '' },
      apoderado: { nombre: '', apellido: '', email: '', contrasena: '' },
    };
  }
}
