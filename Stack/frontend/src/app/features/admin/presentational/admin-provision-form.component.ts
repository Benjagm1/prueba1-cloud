import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Asignatura, Curso, Usuario } from '../../../shared/models/catalog.model';
import { etiquetaAsignatura, etiquetaCurso, etiquetaUsuario } from '../../../shared/utils/labels';

export interface ProvisionFormValue {
  nombre: string;
  apellido: string;
  email: string;
  contrasena: string;
  rol: 'alumno' | 'apoderado' | 'profesor';
  cursoId: number | null;
  apoderadoUsuarioId: number | null;
  alumnoUsuarioId: number | null;
  asignaturaId: number | null;
}

@Component({
  selector: 'app-admin-provision-form',
  standalone: true,
  imports: [FormsModule],
  template: `
    <form (ngSubmit)="onSubmit()" class="provision-form">
      <div class="form-section">
        <p class="form-section__title">Datos personales</p>
        <div class="form-grid form-grid--2">
          <div class="field">
            <label for="nombre">Nombre</label>
            <input id="nombre" [(ngModel)]="value.nombre" name="nombre" required autocomplete="given-name" />
          </div>
          <div class="field">
            <label for="apellido">Apellido</label>
            <input id="apellido" [(ngModel)]="value.apellido" name="apellido" required autocomplete="family-name" />
          </div>
          <div class="field">
            <label for="email">Correo institucional</label>
            <input id="email" type="email" [(ngModel)]="value.email" name="email" required autocomplete="email" />
          </div>
          <div class="field">
            <label for="pass">Contraseña inicial</label>
            <input id="pass" type="password" [(ngModel)]="value.contrasena" name="pass" required minlength="6" />
            <span class="hint">El usuario podrá cambiarla después desde su perfil.</span>
          </div>
        </div>
      </div>

      <div class="form-section">
        <p class="form-section__title">Rol en el colegio</p>
        <div class="form-grid">
          <div class="field">
            <label for="rol">Tipo de cuenta</label>
            <select id="rol" [(ngModel)]="value.rol" name="rol" (ngModelChange)="rolChange.emit($event)">
              <option value="alumno">Alumno</option>
              <option value="apoderado">Apoderado</option>
              <option value="profesor">Profesor</option>
            </select>
          </div>
        </div>
      </div>

      @if (value.rol === 'alumno') {
        <div class="form-section">
          <p class="form-section__title">Inscripción del alumno</p>
          <div class="form-grid form-grid--2">
            <div class="field">
              <label for="curso">Curso</label>
              <select id="curso" [(ngModel)]="value.cursoId" name="curso" required>
                <option [ngValue]="null">Seleccione un curso…</option>
                @for (c of cursos; track c.id) {
                  <option [ngValue]="c.id">{{ cursoLabel(c) }}</option>
                }
              </select>
            </div>
            <div class="field">
              <label for="apoderado">Apoderado responsable</label>
              <select id="apoderado" [(ngModel)]="value.apoderadoUsuarioId" name="apoderado" required>
                <option [ngValue]="null">Seleccione un apoderado…</option>
                @for (a of apoderados; track a.idUsuario) {
                  <option [ngValue]="a.idUsuario">{{ usuarioLabel(a) }}</option>
                }
              </select>
              @if (apoderados.length === 0) {
                <span class="hint">Si el apoderado aún no existe, use la pestaña «Alumno + apoderado» para crear ambos a la vez.</span>
              }
            </div>
          </div>
        </div>
      }

      @if (value.rol === 'apoderado') {
        <div class="form-section">
          <p class="form-section__title">Vínculo con el pupilo</p>
          <div class="field">
            <label for="pupilo">Alumno a cargo</label>
            <select id="pupilo" [(ngModel)]="value.alumnoUsuarioId" name="pupilo" required>
              <option [ngValue]="null">Seleccione un alumno…</option>
              @for (a of alumnos; track a.idUsuario) {
                <option [ngValue]="a.idUsuario">{{ usuarioLabel(a) }}</option>
              }
            </select>
            @if (alumnos.length === 0) {
              <span class="hint">Si el alumno aún no existe, use la pestaña «Alumno + apoderado» para crear ambos a la vez.</span>
            }
          </div>
        </div>
      }

      @if (value.rol === 'profesor') {
        <div class="form-section">
          <p class="form-section__title">Asignación docente</p>
          <div class="form-grid form-grid--2">
            <div class="field">
              <label for="cursoProf">Curso</label>
              <select id="cursoProf" [(ngModel)]="value.cursoId" name="cursoProf" required>
                <option [ngValue]="null">Seleccione un curso…</option>
                @for (c of cursos; track c.id) {
                  <option [ngValue]="c.id">{{ cursoLabel(c) }}</option>
                }
              </select>
            </div>
            <div class="field">
              <label for="asig">Asignatura</label>
              <select id="asig" [(ngModel)]="value.asignaturaId" name="asig" required>
                <option [ngValue]="null">Seleccione una asignatura…</option>
                @for (a of asignaturas; track a.id) {
                  <option [ngValue]="a.id">{{ asignaturaLabel(a) }}</option>
                }
              </select>
            </div>
          </div>
        </div>
      }

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" [disabled]="saving || !formValid()">
          {{ saving ? 'Creando cuenta…' : 'Crear cuenta' }}
        </button>
        <button type="button" class="btn btn-ghost" (click)="resetForm()" [disabled]="saving">Limpiar formulario</button>
      </div>
    </form>
  `,
})
export class AdminProvisionFormComponent {
  @Input() cursos: Curso[] = [];
  @Input() asignaturas: Asignatura[] = [];
  @Input() apoderados: Usuario[] = [];
  @Input() alumnos: Usuario[] = [];
  @Input() saving = false;

  @Output() readonly submitForm = new EventEmitter<ProvisionFormValue>();
  @Output() readonly rolChange = new EventEmitter<string>();

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
    if (v.rol === 'apoderado') {
      return v.alumnoUsuarioId != null;
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
