import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { Asignatura, Curso, Usuario } from '../../../shared/models/catalog.model';
import { badgeClass, etiquetaAsignatura, etiquetaCurso, nombreCompleto, rolLabel } from '../../../shared/utils/labels';
import {
  AdminFamiliaFormComponent,
  FamiliaFormValue,
} from '../presentational/admin-familia-form.component';
import {
  AdminDocenteCargaComponent,
  AsignacionDocenteResumen,
} from '../presentational/admin-docente-carga.component';
import {
  AdminProvisionFormComponent,
  ProvisionFormValue,
} from '../presentational/admin-provision-form.component';

@Component({
  selector: 'app-admin-page',
  standalone: true,
  imports: [
    PageHeaderComponent,
    AdminProvisionFormComponent,
    AdminFamiliaFormComponent,
    AdminDocenteCargaComponent,
    FormsModule,
  ],
  template: `
    <app-page-header
      title="Administración escolar"
      subtitle="Gestione cuentas, cursos y asignaciones del libro de clases digital."
    />

  @if (err()) {
    <div class="alert alert-error" role="alert">{{ err() }}</div>
  }
  @if (msg()) {
    <div class="alert alert-ok" role="status">{{ msg() }}</div>
  }

  <nav class="tabs" aria-label="Secciones de administración">
    @for (t of tabs; track t.id) {
      <button
        type="button"
        class="tabs__btn"
        [class.is-active]="tab() === t.id"
        (click)="selectTab(t.id)"
      >
        {{ t.label }}
      </button>
    }
  </nav>

  @if (tab() === 'familia') {
    <section class="card">
      <div class="card__head">
        <div>
          <h2>Alumno y apoderado (cuentas nuevas)</h2>
          <p>Crea y vincula al pupilo con su apoderado en un solo paso.</p>
        </div>
      </div>
      <app-admin-familia-form
        [cursos]="cursos()"
        [saving]="saving()"
        (submitForm)="provisionarFamilia($event)"
      />
    </section>
  }

  @if (tab() === 'provisionar') {
    <section class="card">
      <div class="card__head">
        <div>
          <h2>Nueva cuenta de usuario</h2>
          <p>Para un solo rol. Si alumno y apoderado son nuevos, use la pestaña «Alumno + apoderado».</p>
        </div>
      </div>
      <app-admin-provision-form
        [cursos]="cursos()"
        [asignaturas]="asignaturas()"
        [apoderados]="apoderados()"
        [alumnos]="alumnos()"
        [saving]="saving()"
        (submitForm)="provisionar($event)"
      />
    </section>
  }

  @if (tab() === 'docentes') {
    <section class="card">
      <div class="card__head">
        <div>
          <h2>Carga docente</h2>
          <p>Asigne cursos y asignaturas a cada profesor (puede tener varios cursos con la misma asignatura).</p>
        </div>
      </div>
      <app-admin-docente-carga
        [profesores]="profesores()"
        [cursos]="cursos()"
        [asignaturas]="asignaturas()"
        [asignaciones]="asignacionesProfesor()"
        [saving]="savingAsignacion()"
        (profesorChange)="onProfesorSeleccionado($event)"
        (agregarAsignacion)="agregarAsignacionDocente($event)"
      />
    </section>
  }

  @if (tab() === 'usuarios') {
    <section class="card">
      <div class="card__head">
        <div>
          <h2>Usuarios registrados</h2>
          <p>{{ usuariosFiltrados().length }} de {{ users().length }} cuentas</p>
        </div>
        <button type="button" class="btn btn-ghost btn-sm" (click)="loadUsers()" [disabled]="loadingUsers()">
          Actualizar lista
        </button>
      </div>
      <div class="toolbar">
        <div class="field">
          <label for="filtroRol">Filtrar por rol</label>
          <select id="filtroRol" [ngModel]="filtroRol()" (ngModelChange)="filtroRol.set($event)" name="filtroRol">
            <option value="todos">Todos los roles</option>
            <option value="alumno">Alumno</option>
            <option value="apoderado">Apoderado</option>
            <option value="profesor">Profesor</option>
            <option value="admin">Administrador</option>
          </select>
        </div>
        <div class="field">
          <label for="ordenRol">Ordenar por</label>
          <select id="ordenRol" [ngModel]="ordenPor()" (ngModelChange)="ordenPor.set($event)" name="ordenRol">
            <option value="rol">Rol, luego apellido</option>
            <option value="apellido">Apellido</option>
            <option value="email">Correo</option>
          </select>
        </div>
      </div>
      @if (usuariosFiltrados().length === 0) {
        <div class="empty-state">
          <p>No hay usuarios que coincidan con el filtro.</p>
        </div>
      } @else {
        <div class="table-wrap">
          <table class="data">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Correo</th>
                <th>Rol</th>
              </tr>
            </thead>
            <tbody>
              @for (u of usuariosFiltrados(); track u.idUsuario) {
                <tr>
                  <td>{{ nombreCompleto(u) }}</td>
                  <td>{{ u.email }}</td>
                  <td><span class="badge" [class]="badgeClass(u.tipo)">{{ rolLabel(u.tipo) }}</span></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </section>
  }

  @if (tab() === 'catalogo') {
    <div class="grid-2">
      <section class="card">
        <div class="card__head">
          <h2>Cursos</h2>
          <p>{{ cursos().length }} niveles</p>
        </div>
        <div class="chip-list">
          @for (c of cursos(); track c.id) {
            <span class="chip"><strong>{{ c.codigo }}</strong> — {{ c.nombre }}</span>
          }
        </div>
      </section>
      <section class="card">
        <div class="card__head">
          <h2>Asignaturas</h2>
          <p>{{ asignaturas().length }} ramos</p>
        </div>
        <div class="chip-list">
          @for (a of asignaturas(); track a.id) {
            <span class="chip">{{ etiquetaAsignatura(a) }}</span>
          }
        </div>
      </section>
    </div>
  }
  `,
})
export class AdminPageContainer implements OnInit {
  @ViewChild(AdminProvisionFormComponent) provisionForm?: AdminProvisionFormComponent;
  @ViewChild(AdminFamiliaFormComponent) familiaForm?: AdminFamiliaFormComponent;

  private readonly http = inject(HttpClient);

  readonly nombreCompleto = nombreCompleto;
  readonly rolLabel = rolLabel;
  readonly badgeClass = badgeClass;
  readonly etiquetaAsignatura = etiquetaAsignatura;

  readonly tabs = [
    { id: 'familia', label: 'Alumno + apoderado' },
    { id: 'provisionar', label: 'Una cuenta' },
    { id: 'docentes', label: 'Carga docente' },
    { id: 'usuarios', label: 'Usuarios' },
    { id: 'catalogo', label: 'Cursos y asignaturas' },
  ];
  readonly tab = signal('familia');
  readonly err = signal('');
  readonly msg = signal('');
  readonly saving = signal(false);
  readonly loadingUsers = signal(false);
  readonly users = signal<Usuario[]>([]);
  readonly cursos = signal<Curso[]>([]);
  readonly asignaturas = signal<Asignatura[]>([]);

  readonly apoderados = computed(() => this.users().filter((u) => u.tipo === 'apoderado'));
  readonly alumnos = computed(() => this.users().filter((u) => u.tipo === 'alumno'));
  readonly profesores = computed(() => this.users().filter((u) => u.tipo === 'profesor'));

  readonly filtroRol = signal('todos');
  readonly ordenPor = signal<'rol' | 'apellido' | 'email'>('rol');
  readonly asignacionesProfesor = signal<AsignacionDocenteResumen[]>([]);
  readonly savingAsignacion = signal(false);
  profesorSeleccionadoId: number | null = null;

  readonly usuariosFiltrados = computed(() => {
    let lista = [...this.users()];
    if (this.filtroRol() !== 'todos') {
      lista = lista.filter((u) => u.tipo === this.filtroRol());
    }
    const cmpRol = (a: Usuario, b: Usuario) => a.tipo.localeCompare(b.tipo);
    const cmpApellido = (a: Usuario, b: Usuario) =>
      a.apellido.localeCompare(b.apellido) || a.nombre.localeCompare(b.nombre);
    const cmpEmail = (a: Usuario, b: Usuario) => a.email.localeCompare(b.email);
    const orden = this.ordenPor();
    if (orden === 'rol') {
      lista.sort((a, b) => cmpRol(a, b) || cmpApellido(a, b));
    } else if (orden === 'apellido') {
      lista.sort(cmpApellido);
    } else {
      lista.sort(cmpEmail);
    }
    return lista;
  });

  ngOnInit(): void {
    void this.loadCatalog();
    void this.loadUsers();
  }

  selectTab(id: string): void {
    this.tab.set(id);
    this.err.set('');
    if (id === 'usuarios') {
      void this.loadUsers();
    }
  }

  async loadUsers(): Promise<void> {
    this.loadingUsers.set(true);
    try {
      const data = await firstValueFrom(this.http.get<Usuario[]>('/auth/usuarios'));
      this.users.set(data);
    } catch {
      this.err.set('No se pudo cargar la lista de usuarios.');
    } finally {
      this.loadingUsers.set(false);
    }
  }

  async loadCatalog(): Promise<void> {
    try {
      const [cursos, asignaturas] = await Promise.all([
        firstValueFrom(this.http.get<Curso[]>('/api/admin/catalogo/cursos')),
        firstValueFrom(this.http.get<Asignatura[]>('/api/admin/catalogo/asignaturas')),
      ]);
      this.cursos.set(cursos);
      this.asignaturas.set(asignaturas);
    } catch {
      this.err.set('No se pudo cargar el catálogo de cursos y asignaturas.');
    }
  }

  async onProfesorSeleccionado(id: number | null): Promise<void> {
    this.profesorSeleccionadoId = id;
    this.asignacionesProfesor.set([]);
    if (id == null) return;
    try {
      const data = await firstValueFrom(
        this.http.get<AsignacionDocenteResumen[]>(`/api/admin/docentes/${id}/asignaciones`),
      );
      this.asignacionesProfesor.set(data);
    } catch {
      this.err.set('No se pudieron cargar las asignaciones del profesor.');
    }
  }

  async agregarAsignacionDocente(ev: { cursoId: number; asignaturaId: number }): Promise<void> {
    if (this.profesorSeleccionadoId == null) return;
    this.savingAsignacion.set(true);
    this.err.set('');
    this.msg.set('');
    try {
      await firstValueFrom(
        this.http.post('/api/admin/docentes/asignaciones', {
          profesorUsuarioId: this.profesorSeleccionadoId,
          cursoId: ev.cursoId,
          asignaturaId: ev.asignaturaId,
        }),
      );
      this.msg.set('Asignación agregada correctamente.');
      await this.onProfesorSeleccionado(this.profesorSeleccionadoId);
    } catch (e: unknown) {
      this.err.set(this.httpError(e));
    } finally {
      this.savingAsignacion.set(false);
    }
  }

  async provisionarFamilia(v: FamiliaFormValue): Promise<void> {
    this.err.set('');
    this.msg.set('');
    this.saving.set(true);
    try {
      const res = await firstValueFrom(
        this.http.post<{ mensaje?: string }>('/api/admin/cuentas/provisionar-familia', {
          cursoId: v.cursoId,
          alumno: v.alumno,
          apoderado: v.apoderado,
        }),
      );
      this.msg.set(res.mensaje ?? 'Alumno y apoderado creados y vinculados.');
      this.familiaForm?.markSuccess();
      await this.loadUsers();
    } catch (e: unknown) {
      this.err.set(this.httpError(e));
    } finally {
      this.saving.set(false);
    }
  }

  async provisionar(v: ProvisionFormValue): Promise<void> {
    this.err.set('');
    this.msg.set('');
    this.saving.set(true);
    const body: Record<string, unknown> = {
      nombre: v.nombre.trim(),
      apellido: v.apellido.trim(),
      email: v.email.trim(),
      contrasena: v.contrasena,
      rol: v.rol,
    };
    if (v.rol === 'alumno') {
      body['cursoId'] = v.cursoId;
      body['apoderadoUsuarioId'] = v.apoderadoUsuarioId;
    } else if (v.rol === 'apoderado') {
      body['alumnoUsuarioId'] = v.alumnoUsuarioId;
    } else if (v.rol === 'profesor') {
      body['cursoId'] = v.cursoId;
      body['asignaturaId'] = v.asignaturaId;
    }
    try {
      const res = await firstValueFrom(
        this.http.post<{ estadoCuenta?: { detalle?: string } }>('/api/admin/cuentas/provisionar', body),
      );
      this.msg.set(res.estadoCuenta?.detalle ?? 'La cuenta se creó correctamente.');
      this.provisionForm?.markSuccess();
      await this.loadUsers();
    } catch (e: unknown) {
      this.err.set(this.httpError(e));
    } finally {
      this.saving.set(false);
    }
  }

  private httpError(e: unknown): string {
    if (e instanceof HttpErrorResponse) {
      if (typeof e.error === 'string' && e.error) return e.error;
      if (e.error && typeof e.error === 'object' && 'message' in e.error) {
        return String((e.error as { message: string }).message);
      }
    }
    return 'No se pudo completar la operación. Verifique los datos e intente de nuevo.';
  }
}
