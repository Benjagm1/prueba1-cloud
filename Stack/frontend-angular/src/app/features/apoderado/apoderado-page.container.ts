import { HttpClient } from '@angular/common/http';
import { JsonPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-apoderado-page',
  standalone: true,
  imports: [FormsModule, JsonPipe],
  template: `
    <div class="card">
      <h2>Pupilo</h2>
      <div class="form-grid">
        <label>alumnoUsuarioId <input type="number" [(ngModel)]="alumnoId" name="aid" /></label>
        <label>nombre <input [(ngModel)]="nombre" name="nom" /></label>
        <label>apellido <input [(ngModel)]="apellido" name="ape" /></label>
        <button type="button" class="btn btn-primary" (click)="actualizarDatos()">Actualizar datos personales</button>
        <button type="button" class="btn btn-ghost" (click)="loadNotas()">Ver notas</button>
        <button type="button" class="btn btn-ghost" (click)="loadAsist()">Ver asistencia</button>
      </div>
      <pre class="json">{{ data() | json }}</pre>
    </div>
  `,
})
export class ApoderadoPageContainer {
  private readonly http = inject(HttpClient);
  alumnoId: number | null = null;
  nombre = '';
  apellido = '';
  readonly data = signal<unknown>(null);

  async actualizarDatos(): Promise<void> {
    await firstValueFrom(
      this.http.put(`/api/apoderados/pupilos/${this.alumnoId}/datos-personales`, {
        nombre: this.nombre,
        apellido: this.apellido,
      }),
    );
    this.data.set({ ok: 'Datos actualizados' });
  }

  async loadNotas(): Promise<void> {
    this.data.set(await firstValueFrom(this.http.get(`/api/academico/alumnos/${this.alumnoId}/notas`)));
  }

  async loadAsist(): Promise<void> {
    const hasta = new Date().toISOString().slice(0, 10);
    const d = new Date();
    d.setMonth(d.getMonth() - 3);
    const desde = d.toISOString().slice(0, 10);
    this.data.set(
      await firstValueFrom(
        this.http.get(`/api/asistencia/alumnos/${this.alumnoId}/historial?desde=${desde}&hasta=${hasta}`),
      ),
    );
  }
}
