import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Curso } from '../../../shared/models/catalog.model';
import { etiquetaCurso } from '../../../shared/utils/labels';

export interface FamiliaFormValue {
  alumno: { nombre: string; apellido: string; email: string; contrasena: string };
  apoderado: { nombre: string; apellido: string; email: string; contrasena: string };
  cursoId: number | null;
}

@Component({
  selector: 'app-admin-familia-form',
  standalone: true,
  imports: [FormsModule],
  template: `
    <form (ngSubmit)="onSubmit()" class="provision-form">
      <div class="alert" style="background: var(--brand-soft); border: 1px solid rgba(0,122,90,0.2); color: var(--brand-dark); margin-bottom: 1.25rem;">
        Use esta opción cuando el alumno y su apoderado son cuentas nuevas. Se crean ambas y se vinculan automáticamente.
      </div>

      <div class="form-section">
        <p class="form-section__title">Curso del alumno</p>
        <div class="field" style="max-width: 420px">
          <label for="cursoFam">Curso</label>
          <select id="cursoFam" [(ngModel)]="value.cursoId" name="cursoFam" required>
            <option [ngValue]="null">Seleccione un curso…</option>
            @for (c of cursos; track c.id) {
              <option [ngValue]="c.id">{{ cursoLabel(c) }}</option>
            }
          </select>
        </div>
      </div>

      <div class="grid-2">
        <div class="form-section">
          <p class="form-section__title">Datos del alumno</p>
          <div class="form-grid">
            <div class="field">
              <label for="an">Nombre</label>
              <input id="an" [(ngModel)]="value.alumno.nombre" name="an" required />
            </div>
            <div class="field">
              <label for="aa">Apellido</label>
              <input id="aa" [(ngModel)]="value.alumno.apellido" name="aa" required />
            </div>
            <div class="field">
              <label for="ae">Correo</label>
              <input id="ae" type="email" [(ngModel)]="value.alumno.email" name="ae" required />
            </div>
            <div class="field">
              <label for="ap">Contraseña</label>
              <input id="ap" type="password" [(ngModel)]="value.alumno.contrasena" name="ap" required minlength="6" />
            </div>
          </div>
        </div>

        <div class="form-section">
          <p class="form-section__title">Datos del apoderado</p>
          <div class="form-grid">
            <div class="field">
              <label for="pn">Nombre</label>
              <input id="pn" [(ngModel)]="value.apoderado.nombre" name="pn" required />
            </div>
            <div class="field">
              <label for="pa">Apellido</label>
              <input id="pa" [(ngModel)]="value.apoderado.apellido" name="pa" required />
            </div>
            <div class="field">
              <label for="pe">Correo</label>
              <input id="pe" type="email" [(ngModel)]="value.apoderado.email" name="pe" required />
            </div>
            <div class="field">
              <label for="pp">Contraseña</label>
              <input id="pp" type="password" [(ngModel)]="value.apoderado.contrasena" name="pp" required minlength="6" />
            </div>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" [disabled]="saving || !valid()">
          {{ saving ? 'Creando familia…' : 'Crear alumno y apoderado' }}
        </button>
        <button type="button" class="btn btn-ghost" (click)="reset()" [disabled]="saving">Limpiar</button>
      </div>
    </form>
  `,
})
export class AdminFamiliaFormComponent {
  @Input() cursos: Curso[] = [];
  @Input() saving = false;
  @Output() readonly submitForm = new EventEmitter<FamiliaFormValue>();

  readonly cursoLabel = etiquetaCurso;

  value: FamiliaFormValue = this.empty();

  onSubmit(): void {
    if (!this.valid()) return;
    this.submitForm.emit({ ...this.value, alumno: { ...this.value.alumno }, apoderado: { ...this.value.apoderado } });
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
