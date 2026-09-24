import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { PageHeaderComponent } from '../../shared/components/page-header.component';
import { Calificacion } from '../../shared/models/catalog.model';
import { nombreCompleto } from '../../shared/utils/labels';

interface PupiloResumen {
  alumnoUsuarioId: number;
  nombre: string;
  apellido: string;
}

@Component({
  selector: 'app-apoderado-page',
  standalone: true,
  imports: [FormsModule, PageHeaderComponent, DatePipe],
  template: `
    <app-page-header
      title="Seguimiento del pupilo"
      subtitle="Consulte notas y asistencia, y actualice los datos personales de su hijo o hija."
    />

    @if (err()) {
      <div class="alert alert-error" role="alert">{{ err() }}</div>
    }
    @if (msg()) {
      <div class="alert alert-ok" role="status">{{ msg() }}</div>
    }

    <section class="card">
      <div class="card__head">
        <h2>Seleccionar pupilo</h2>
      </div>
      @if (pupilos().length === 0) {
        <div class="empty-state">
          <p>No tiene alumnos vinculados. Si cree que es un error, contacte a administración.</p>
        </div>
      } @else {
        <div class="field" style="max-width: 480px">
          <label for="pupilo">Alumno a cargo</label>
          <select id="pupilo" [(ngModel)]="alumnoId" name="pupilo" (ngModelChange)="onPupiloChange()">
            <option [ngValue]="null">Seleccione un alumno…</option>
            @for (p of pupilos(); track p.alumnoUsuarioId) {
              <option [ngValue]="p.alumnoUsuarioId">{{ nombreCompleto(p) }}</option>
            }
          </select>
        </div>
      }
    </section>

    @if (alumnoId != null) {
      <div class="grid-2">
        <section class="card">
          <div class="card__head">
            <h2>Datos personales</h2>
            <p>{{ nombreCompletoSeleccionado() }}</p>
          </div>
          <div class="form-grid form-grid--2">
            <div class="field">
              <label for="nom">Nombre</label>
              <input id="nom" [(ngModel)]="nombre" name="nom" />
            </div>
            <div class="field">
              <label for="ape">Apellido</label>
              <input id="ape" [(ngModel)]="apellido" name="ape" />
            </div>
          </div>
          <div class="form-actions">
            <button type="button" class="btn btn-primary" (click)="actualizarDatos()">Guardar cambios</button>
          </div>
        </section>

        <section class="card">
          <div class="card__head">
            <h2>Información académica</h2>
          </div>
          <div class="form-actions" style="border: none; padding-top: 0">
            <button type="button" class="btn btn-ghost" (click)="loadNotas()">Ver notas</button>
            <button type="button" class="btn btn-ghost" (click)="loadAsist()">Ver asistencia</button>
          </div>
        </section>
      </div>

      @if (notas().length > 0) {
        <section class="card">
          <div class="card__head"><h2>Calificaciones</h2></div>
          <div class="table-wrap">
            <table class="data">
              <thead>
                <tr><th>Evaluación</th><th>Nota</th><th>Fecha</th></tr>
              </thead>
              <tbody>
                @for (n of notas(); track n.id) {
                  <tr>
                    <td>{{ n.nombreEvaluacion }}</td>
                    <td><strong>{{ n.nota }}</strong></td>
                    <td>{{ n.creadoEn | date: 'dd/MM/yyyy' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </section>
      }

      @if (asistLineas().length > 0) {
        <section class="card">
          <div class="card__head"><h2>Historial de asistencia</h2></div>
          <div class="table-wrap">
            <table class="data">
              <thead>
                <tr><th>Fecha</th><th>Estado</th></tr>
              </thead>
              <tbody>
                @for (f of asistLineas(); track f.fecha) {
                  <tr>
                    <td>{{ f.fecha }}</td>
                    <td>{{ f.presente ? 'Presente' : 'Ausente' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </section>
      }
    }
  `,
})
export class ApoderadoPageContainer implements OnInit {
  private readonly http = inject(HttpClient);

  readonly nombreCompleto = nombreCompleto;
  alumnoId: number | null = null;
  nombre = '';
  apellido = '';
  readonly err = signal('');
  readonly msg = signal('');
  readonly notas = signal<Calificacion[]>([]);
  readonly asistLineas = signal<{ fecha: string; presente: boolean }[]>([]);
  readonly pupilos = signal<PupiloResumen[]>([]);

  ngOnInit(): void {
    void this.loadPupilos();
  }

  async loadPupilos(): Promise<void> {
    try {
      const data = await firstValueFrom(this.http.get<PupiloResumen[]>('/api/apoderados/mis-pupilos'));
      this.pupilos.set(data);
      if (data.length === 1) {
        this.alumnoId = data[0].alumnoUsuarioId;
        this.nombre = data[0].nombre;
        this.apellido = data[0].apellido;
      }
    } catch {
      this.err.set('No se pudo cargar la lista de pupilos.');
    }
  }

  onPupiloChange(): void {
    this.notas.set([]);
    this.asistLineas.set([]);
    this.err.set('');
    const p = this.pupilos().find((x) => x.alumnoUsuarioId === this.alumnoId);
    if (p) {
      this.nombre = p.nombre;
      this.apellido = p.apellido;
    }
  }

  nombreCompletoSeleccionado(): string {
    const p = this.pupilos().find((x) => x.alumnoUsuarioId === this.alumnoId);
    return p ? nombreCompleto(p) : '';
  }

  async actualizarDatos(): Promise<void> {
    this.err.set('');
    this.msg.set('');
    try {
      await firstValueFrom(
        this.http.put(`/api/apoderados/pupilos/${this.alumnoId}/datos-personales`, {
          nombre: this.nombre,
          apellido: this.apellido,
        }),
      );
      this.msg.set('Datos actualizados correctamente.');
      await this.loadPupilos();
    } catch {
      this.err.set('No se pudieron guardar los datos.');
    }
  }

  async loadNotas(): Promise<void> {
    this.err.set('');
    try {
      const data = await firstValueFrom(
        this.http.get<Calificacion[]>(`/api/academico/alumnos/${this.alumnoId}/notas`),
      );
      this.notas.set(data);
    } catch {
      this.err.set('No tiene permiso para ver las notas de este alumno.');
      this.notas.set([]);
    }
  }

  async loadAsist(): Promise<void> {
    this.err.set('');
    const hasta = new Date().toISOString().slice(0, 10);
    const d = new Date();
    d.setMonth(d.getMonth() - 3);
    const desde = d.toISOString().slice(0, 10);
    try {
      const data = await firstValueFrom(
        this.http.get<{ fecha: string; presente: boolean }[]>(
          `/api/asistencia/alumnos/${this.alumnoId}/historial?desde=${desde}&hasta=${hasta}`,
        ),
      );
      this.asistLineas.set(Array.isArray(data) ? data : []);
    } catch {
      this.err.set('No se pudo cargar la asistencia.');
      this.asistLineas.set([]);
    }
  }
}
