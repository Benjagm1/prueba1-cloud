import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import {
  AsignacionDocenteResumen,
  Asignatura,
  AuditoriaEvento,
  Curso,
  Usuario,
} from '../../core/models/admin.model';
import { AdminApiService } from '../../core/services/admin-api.service';
import {
  badgeClass,
  esCursoBasica,
  esCursoMedia,
  etiquetaAsignatura,
  nombreCompleto,
  rolLabel,
} from '../../core/utils/admin-labels';
import { AdminDocenteCarga } from '../presentational/admin-docente-carga';
import { AdminFamiliaForm } from '../presentational/admin-familia-form';
import { AdminProvisionForm } from '../presentational/admin-provision-form';
import { FamiliaFormValue, ProvisionFormValue } from '../../core/models/admin.model';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [FormsModule, AdminFamiliaForm, AdminProvisionForm, AdminDocenteCarga],
  templateUrl: './admin-panel.html',
  styleUrl: './admin-panel.css',
})
export class AdminPanel implements OnInit {
  @ViewChild(AdminProvisionForm) provisionForm?: AdminProvisionForm;
  @ViewChild(AdminFamiliaForm) familiaForm?: AdminFamiliaForm;

  private readonly api = inject(AdminApiService);

  readonly nombreCompleto = nombreCompleto;
  readonly rolLabel = rolLabel;
  readonly badgeClass = badgeClass;
  readonly etiquetaAsignatura = etiquetaAsignatura;

  readonly tabs = [
    { id: 'familia', label: 'Alumno + apoderado' },
    { id: 'provisionar', label: 'Una cuenta' },
    { id: 'docentes', label: 'Carga docente' },
    { id: 'usuarios', label: 'Usuarios' },
    { id: 'historial', label: 'Historial' },
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
  readonly auditoria = signal<AuditoriaEvento[]>([]);
  readonly loadingAuditoria = signal(false);

  readonly cursosMedia = computed(() => this.cursos().filter(esCursoMedia));
  readonly cursosBasica = computed(() => this.cursos().filter(esCursoBasica));

  readonly apoderados = computed(() => this.users().filter((u) => u.tipo === 'apoderado'));
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
    if (id === 'historial') {
      void this.loadAuditoria();
    }
  }

  formatFecha(iso: string): string {
    try {
      return new Date(iso).toLocaleString('es-CL', {
        dateStyle: 'short',
        timeStyle: 'short',
      });
    } catch {
      return iso;
    }
  }

  async loadUsers(): Promise<void> {
    this.loadingUsers.set(true);
    try {
      const data = await firstValueFrom(this.api.listarUsuarios());
      this.users.set(data);
    } catch {
      this.err.set('No se pudo cargar la lista de usuarios.');
    } finally {
      this.loadingUsers.set(false);
    }
  }

  async loadAuditoria(): Promise<void> {
    this.loadingAuditoria.set(true);
    try {
      const data = await firstValueFrom(this.api.listarAuditoria(200));
      this.auditoria.set(data);
    } catch {
      this.err.set('No se pudo cargar el historial de actividad.');
    } finally {
      this.loadingAuditoria.set(false);
    }
  }

  async loadCatalog(): Promise<void> {
    try {
      const [cursos, asignaturas] = await Promise.all([
        firstValueFrom(this.api.listarCursos()),
        firstValueFrom(this.api.listarAsignaturas()),
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
      const data = await firstValueFrom(this.api.asignacionesProfesor(id));
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
        this.api.agregarAsignacion(this.profesorSeleccionadoId, ev.cursoId, ev.asignaturaId),
      );
      this.msg.set('Asignación agregada correctamente.');
      await this.onProfesorSeleccionado(this.profesorSeleccionadoId);
      if (this.tab() === 'historial') await this.loadAuditoria();
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
      const res = await firstValueFrom(this.api.provisionarFamilia(v));
      this.msg.set(res.mensaje ?? 'Alumno y apoderado creados y vinculados.');
      this.familiaForm?.markSuccess();
      await this.loadUsers();
      if (this.tab() === 'historial') await this.loadAuditoria();
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
    try {
      const res = await firstValueFrom(this.api.provisionar(v));
      this.msg.set(res.estadoCuenta?.detalle ?? 'La cuenta se creó correctamente.');
      this.provisionForm?.markSuccess();
      await this.loadUsers();
      if (this.tab() === 'historial') await this.loadAuditoria();
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
