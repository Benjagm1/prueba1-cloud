import { JsonPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

interface UsuarioRow {
  idUsuario: number;
  nombre: string;
  apellido: string;
  email: string;
  tipo: string;
}

@Component({
  selector: 'app-admin-page',
  standalone: true,
  imports: [FormsModule, JsonPipe],
  template: `
    <div class="row-actions">
      @for (t of tabs; track t.id) {
        <button type="button" class="btn" [class.btn-primary]="tab() === t.id" [class.btn-ghost]="tab() !== t.id" (click)="tab.set(t.id)">
          {{ t.label }}
        </button>
      }
    </div>
    @if (err()) {
      <div class="alert alert-error">{{ err() }}</div>
    }
    @if (msg()) {
      <div class="alert alert-ok">{{ msg() }}</div>
    }

    @if (tab() === 'provisionar') {
      <div class="card">
        <h2>Crear cuenta completa</h2>
        <p class="muted">POST /api/admin/cuentas/provisionar — usuario + rol + vínculos obligatorios.</p>
        <div class="form-grid">
          <label>nombre <input [(ngModel)]="prov.nombre" name="pn" /></label>
          <label>apellido <input [(ngModel)]="prov.apellido" name="pa" /></label>
          <label>email <input type="email" [(ngModel)]="prov.email" name="pe" /></label>
          <label>contraseña <input type="password" [(ngModel)]="prov.contrasena" name="pc" /></label>
          <label>rol
            <select [(ngModel)]="prov.rol" name="pr">
              <option value="alumno">alumno</option>
              <option value="apoderado">apoderado</option>
              <option value="profesor">profesor</option>
            </select>
          </label>
          @if (prov.rol === 'alumno') {
            <label>cursoId <input type="number" [(ngModel)]="prov.cursoId" name="pcur" /></label>
            <label>apoderadoUsuarioId <input type="number" [(ngModel)]="prov.apoderadoUsuarioId" name="pap" /></label>
          }
          @if (prov.rol === 'apoderado') {
            <label>alumnoUsuarioId (pupilo) <input type="number" [(ngModel)]="prov.alumnoUsuarioId" name="pal" /></label>
          }
          @if (prov.rol === 'profesor') {
            <label>cursoId <input type="number" [(ngModel)]="prov.cursoId" name="pcur2" /></label>
            <label>asignaturaId <input type="number" [(ngModel)]="prov.asignaturaId" name="pasig" /></label>
          }
          <button type="button" class="btn btn-primary" (click)="provisionar()">Crear cuenta</button>
        </div>
      </div>
    }

    @if (tab() === 'usuarios') {
      <div class="card">
        <h2>Usuarios</h2>
        <button type="button" class="btn btn-ghost" (click)="loadUsers()">Actualizar</button>
        <table class="data">
          <thead>
            <tr><th>id</th><th>nombre</th><th>email</th><th>tipo</th></tr>
          </thead>
          <tbody>
            @for (u of users(); track u.idUsuario) {
              <tr>
                <td>{{ u.idUsuario }}</td>
                <td>{{ u.nombre }} {{ u.apellido }}</td>
                <td>{{ u.email }}</td>
                <td>{{ u.tipo }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }

    @if (tab() === 'catalogo') {
      <div class="card">
        <h2>Catálogo</h2>
        <p><strong>Cursos:</strong> {{ cursos().length }}</p>
        <pre class="json">{{ cursos() | json }}</pre>
        <p><strong>Asignaturas:</strong> {{ asignaturas().length }}</p>
        <pre class="json">{{ asignaturas() | json }}</pre>
      </div>
    }
  `,
})
export class AdminPageContainer implements OnInit {
  private readonly http = inject(HttpClient);

  readonly tabs = [
    { id: 'provisionar', label: 'Crear cuenta' },
    { id: 'usuarios', label: 'Usuarios' },
    { id: 'catalogo', label: 'Catálogo' },
  ];
  readonly tab = signal('provisionar');
  readonly err = signal('');
  readonly msg = signal('');
  readonly users = signal<UsuarioRow[]>([]);
  readonly cursos = signal<unknown[]>([]);
  readonly asignaturas = signal<unknown[]>([]);

  prov = {
    nombre: '',
    apellido: '',
    email: '',
    contrasena: '',
    rol: 'alumno',
    cursoId: null as number | null,
    apoderadoUsuarioId: null as number | null,
    alumnoUsuarioId: null as number | null,
    asignaturaId: null as number | null,
  };

  ngOnInit(): void {
    void this.loadCatalog();
    void this.loadUsers();
  }

  async loadUsers(): Promise<void> {
    try {
      const data = await firstValueFrom(this.http.get<UsuarioRow[]>('/auth/usuarios'));
      this.users.set(data);
    } catch {
      this.err.set('No se pudieron cargar usuarios');
    }
  }

  async loadCatalog(): Promise<void> {
    try {
      const [cursos, asignaturas] = await Promise.all([
        firstValueFrom(this.http.get<unknown[]>('/api/admin/catalogo/cursos')),
        firstValueFrom(this.http.get<unknown[]>('/api/admin/catalogo/asignaturas')),
      ]);
      this.cursos.set(cursos);
      this.asignaturas.set(asignaturas);
    } catch {
      this.err.set('No se pudo cargar catálogo');
    }
  }

  async provisionar(): Promise<void> {
    this.err.set('');
    this.msg.set('');
    const body: Record<string, unknown> = {
      nombre: this.prov.nombre,
      apellido: this.prov.apellido,
      email: this.prov.email,
      contrasena: this.prov.contrasena,
      rol: this.prov.rol,
    };
    if (this.prov.rol === 'alumno') {
      body['cursoId'] = this.prov.cursoId;
      body['apoderadoUsuarioId'] = this.prov.apoderadoUsuarioId;
    } else if (this.prov.rol === 'apoderado') {
      body['alumnoUsuarioId'] = this.prov.alumnoUsuarioId;
    } else if (this.prov.rol === 'profesor') {
      body['cursoId'] = this.prov.cursoId;
      body['asignaturaId'] = this.prov.asignaturaId;
    }
    try {
      const res = await firstValueFrom(
        this.http.post<{ estadoCuenta?: { detalle?: string } }>('/api/admin/cuentas/provisionar', body),
      );
      this.msg.set(res.estadoCuenta?.detalle ?? 'Cuenta creada');
      await this.loadUsers();
    } catch (e: unknown) {
      this.err.set(this.httpError(e));
    }
  }

  private httpError(e: unknown): string {
    if (e && typeof e === 'object' && 'error' in e) {
      const err = (e as { error: unknown }).error;
      if (typeof err === 'string') return err;
      if (err && typeof err === 'object' && 'message' in err) {
        return String((err as { message: string }).message);
      }
    }
    return 'Error en la petición';
  }
}
