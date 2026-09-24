import { HttpClient } from '@angular/common/http';
import { JsonPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-alumno-page',
  standalone: true,
  imports: [JsonPipe],
  template: `
    <div class="card">
      <h2>Mis notas</h2>
      <button type="button" class="btn btn-primary" (click)="loadNotas()">Cargar</button>
      <pre class="json">{{ notas() | json }}</pre>
    </div>
    <div class="card">
      <h2>Mi asistencia</h2>
      <button type="button" class="btn btn-primary" (click)="loadAsist()">Resumen</button>
      <pre class="json">{{ asist() | json }}</pre>
    </div>
  `,
})
export class AlumnoPageContainer {
  private readonly http = inject(HttpClient);
  readonly notas = signal<unknown>(null);
  readonly asist = signal<unknown>(null);

  async loadNotas(): Promise<void> {
    this.notas.set(await firstValueFrom(this.http.get('/api/academico/mis-notas')));
  }

  async loadAsist(): Promise<void> {
    const hasta = new Date().toISOString().slice(0, 10);
    const d = new Date();
    d.setMonth(d.getMonth() - 3);
    const desde = d.toISOString().slice(0, 10);
    this.asist.set(
      await firstValueFrom(this.http.get(`/api/asistencia/mi-resumen?desde=${desde}&hasta=${hasta}`)),
    );
  }
}
