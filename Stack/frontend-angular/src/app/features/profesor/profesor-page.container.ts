import { HttpClient } from '@angular/common/http';
import { JsonPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-profesor-page',
  standalone: true,
  imports: [FormsModule, JsonPipe],
  template: `
    <div class="card">
      <h2>Lista de alumnos</h2>
      <div class="form-grid">
        <label>cursoId <input type="number" [(ngModel)]="cursoId" name="c" /></label>
        <label>asignaturaId <input type="number" [(ngModel)]="asignaturaId" name="a" /></label>
        <button type="button" class="btn btn-primary" (click)="lista()">Cargar</button>
      </div>
      <pre class="json">{{ data() | json }}</pre>
    </div>
  `,
})
export class ProfesorPageContainer {
  private readonly http = inject(HttpClient);
  cursoId: number | null = null;
  asignaturaId: number | null = null;
  readonly data = signal<unknown>(null);

  async lista(): Promise<void> {
    this.data.set(
      await firstValueFrom(
        this.http.get(`/api/docentes/lista-alumnos?cursoId=${this.cursoId}&asignaturaId=${this.asignaturaId}`),
      ),
    );
  }
}
