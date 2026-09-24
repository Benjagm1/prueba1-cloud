import { HttpClient } from '@angular/common/http';
import { JsonPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-mensajes-page',
  standalone: true,
  imports: [FormsModule, JsonPipe],
  template: `
    <div class="card">
      <h2>Enviar mensaje</h2>
      <div class="form-grid">
        <label>destinatarioUsuarioId <input type="number" [(ngModel)]="destId" name="dest" /></label>
        <label>asunto <input [(ngModel)]="asunto" name="asunto" /></label>
        <label>cuerpo <textarea rows="3" [(ngModel)]="cuerpo" name="cuerpo"></textarea></label>
        <button type="button" class="btn btn-primary" (click)="enviar()">Enviar</button>
      </div>
    </div>
    <div class="card">
      <h2>Recibidos</h2>
      <pre class="json">{{ recibidos() | json }}</pre>
    </div>
    <div class="card">
      <h2>Notificaciones</h2>
      <pre class="json">{{ notificaciones() | json }}</pre>
    </div>
    @if (err()) {
      <div class="alert alert-error">{{ err() }}</div>
    }
  `,
})
export class MensajesPageContainer implements OnInit {
  private readonly http = inject(HttpClient);
  readonly recibidos = signal<unknown[]>([]);
  readonly notificaciones = signal<unknown[]>([]);
  readonly err = signal('');
  destId: number | null = null;
  asunto = '';
  cuerpo = '';

  ngOnInit(): void {
    void this.reload();
  }

  async reload(): Promise<void> {
    try {
      const [r, n] = await Promise.all([
        firstValueFrom(this.http.get<unknown[]>('/api/mensajes/recibidos')),
        firstValueFrom(this.http.get<unknown[]>('/api/mensajes/mis-notificaciones')),
      ]);
      this.recibidos.set(r);
      this.notificaciones.set(n);
    } catch {
      this.err.set('No se pudo cargar mensajes');
    }
  }

  async enviar(): Promise<void> {
    this.err.set('');
    try {
      await firstValueFrom(
        this.http.post('/api/mensajes/enviar', {
          destinatarioUsuarioId: this.destId,
          asunto: this.asunto,
          cuerpo: this.cuerpo,
        }),
      );
      this.cuerpo = '';
      await this.reload();
    } catch (e: unknown) {
      this.err.set('No se pudo enviar (revise permisos entre roles)');
    }
  }
}
