import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { PageHeaderComponent } from '../../shared/components/page-header.component';
import { Calificacion } from '../../shared/models/catalog.model';

@Component({
  selector: 'app-alumno-page',
  standalone: true,
  imports: [PageHeaderComponent, DatePipe, DecimalPipe],
  template: `
    <app-page-header
      title="Mi rendimiento académico"
      subtitle="Consulte sus calificaciones y el resumen de asistencia del período."
    />

    <div class="grid-2">
      <section class="card">
        <div class="card__head">
          <h2>Calificaciones</h2>
          <button type="button" class="btn btn-ghost btn-sm" (click)="loadNotas()" [disabled]="loadingNotas()">
            Actualizar
          </button>
        </div>
        @if (loadingNotas()) {
          <p class="muted">Cargando…</p>
        } @else if (notas().length === 0) {
          <div class="empty-state"><p>Aún no hay notas registradas.</p></div>
        } @else {
          <div class="table-wrap">
            <table class="data">
              <thead>
                <tr>
                  <th>Evaluación</th>
                  <th>Nota</th>
                  <th>Fecha</th>
                </tr>
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
        }
      </section>

      <section class="card">
        <div class="card__head">
          <h2>Asistencia (últimos 3 meses)</h2>
          <button type="button" class="btn btn-ghost btn-sm" (click)="loadAsist()" [disabled]="loadingAsist()">
            Actualizar
          </button>
        </div>
        @if (loadingAsist()) {
          <p class="muted">Cargando…</p>
        } @else if (!asistResumen()) {
          <div class="empty-state"><p>Sin datos de asistencia.</p></div>
        } @else {
          <div class="stat-row">
            <div class="stat">
              <div class="stat__value">{{ asistResumen()?.porcentaje | number: '1.0-0' }}%</div>
              <div class="stat__label">Asistencia</div>
            </div>
            <div class="stat">
              <div class="stat__value">{{ asistResumen()?.diasPresentes ?? '—' }}</div>
              <div class="stat__label">Días presente</div>
            </div>
            <div class="stat">
              <div class="stat__value">{{ asistResumen()?.diasRegistrados ?? '—' }}</div>
              <div class="stat__label">Días registrados</div>
            </div>
          </div>
          @if (asistResumen()?.bajoUmbral85) {
            <div class="alert alert-error" style="margin-top: 1rem">
              Su asistencia está por debajo del 85%. Consulte con su profesor jefe.
            </div>
          }
        }
      </section>
    </div>
  `,
})
export class AlumnoPageContainer implements OnInit {
  private readonly http = inject(HttpClient);
  readonly notas = signal<Calificacion[]>([]);
  readonly asistResumen = signal<{
    porcentaje?: number;
    diasPresentes?: number;
    diasRegistrados?: number;
    bajoUmbral85?: boolean;
  } | null>(null);
  readonly loadingNotas = signal(false);
  readonly loadingAsist = signal(false);

  ngOnInit(): void {
    void this.loadNotas();
    void this.loadAsist();
  }

  async loadNotas(): Promise<void> {
    this.loadingNotas.set(true);
    try {
      const data = await firstValueFrom(this.http.get<Calificacion[]>('/api/academico/mis-notas'));
      this.notas.set(data);
    } finally {
      this.loadingNotas.set(false);
    }
  }

  async loadAsist(): Promise<void> {
    this.loadingAsist.set(true);
    const hasta = new Date().toISOString().slice(0, 10);
    const d = new Date();
    d.setMonth(d.getMonth() - 3);
    const desde = d.toISOString().slice(0, 10);
    try {
      const data = await firstValueFrom(
        this.http.get<{
          porcentaje?: number;
          diasPresentes?: number;
          diasRegistrados?: number;
          bajoUmbral85?: boolean;
        }>(`/api/asistencia/mi-resumen?desde=${desde}&hasta=${hasta}`),
      );
      this.asistResumen.set(data);
    } finally {
      this.loadingAsist.set(false);
    }
  }
}
