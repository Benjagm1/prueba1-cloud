import { DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { PageHeaderComponent } from '../../../shared/components/page-header.component';
import { AuthService } from '../../../core/services/auth.service';
import { ContactoMensajeria, etiquetaContacto } from '../../../shared/models/contacto.model';
import { Mensaje, Notificacion, Usuario } from '../../../shared/models/catalog.model';

@Component({
  selector: 'app-mensajes-page',
  standalone: true,
  imports: [FormsModule, PageHeaderComponent, DatePipe],
  template: `
    <app-page-header
      title="Mensajes y notificaciones"
      subtitle="Comuníquese con otros miembros de la comunidad escolar y revise avisos institucionales."
    />

    @if (err()) {
      <div class="alert alert-error" role="alert">{{ err() }}</div>
    }

    <div class="grid-2">
      <section class="card">
        <div class="card__head">
          <h2>Redactar mensaje</h2>
        </div>
        <div class="form-grid">
          @if (contactos().length > 0) {
            <div class="field">
              <label for="dest">Destinatario</label>
              <select id="dest" [(ngModel)]="destId" name="dest">
                <option [ngValue]="null">Seleccione destinatario…</option>
                @for (c of contactos(); track c.usuarioId) {
                  <option [ngValue]="c.usuarioId">{{ etiquetaContacto(c) }}</option>
                }
              </select>
            </div>
          } @else if (!cargandoContactos()) {
            <p class="muted">No hay destinatarios disponibles para su perfil.</p>
          }
          <div class="field">
            <label for="asunto">Asunto</label>
            <input id="asunto" [(ngModel)]="asunto" name="asunto" placeholder="Ej. Consulta sobre evaluación" />
          </div>
          <div class="field">
            <label for="cuerpo">Mensaje</label>
            <textarea id="cuerpo" rows="4" [(ngModel)]="cuerpo" name="cuerpo" required></textarea>
          </div>
        </div>
        <div class="form-actions">
          <button
            type="button"
            class="btn btn-primary"
            (click)="enviar()"
            [disabled]="!cuerpo.trim() || destId == null || contactos().length === 0"
          >
            Enviar mensaje
          </button>
        </div>
      </section>

      <section class="card">
        <div class="card__head">
          <h2>Notificaciones</h2>
          <p>{{ notificaciones().length }} en total</p>
        </div>
        @if (notificaciones().length === 0) {
          <div class="empty-state"><p>No tiene notificaciones.</p></div>
        } @else {
          <ul class="msg-list">
            @for (n of notificaciones(); track n.id) {
              <li class="msg-item">
                <div class="msg-item__head">
                  <span class="msg-item__title">{{ n.titulo }}</span>
                  <span class="badge" [class.badge--nueva]="!n.leida" [class.badge--leida]="n.leida">
                    {{ n.leida ? 'Leída' : 'Nueva' }}
                  </span>
                </div>
                <p class="msg-item__body">{{ n.cuerpo }}</p>
                <span class="msg-item__meta">{{ n.creadoEn | date: 'dd/MM/yyyy HH:mm' }}</span>
              </li>
            }
          </ul>
        }
      </section>
    </div>

    <section class="card">
      <div class="card__head">
        <h2>Bandeja de entrada</h2>
        <button type="button" class="btn btn-ghost btn-sm" (click)="reload()">Actualizar</button>
      </div>
      @if (recibidos().length === 0) {
        <div class="empty-state"><p>No hay mensajes recibidos.</p></div>
      } @else {
        <ul class="msg-list">
          @for (m of recibidos(); track m.id) {
            <li class="msg-item">
              <div class="msg-item__head">
                <span class="msg-item__title">{{ m.asunto }}</span>
                <span class="badge" [class.badge--nueva]="!m.leido" [class.badge--leida]="m.leido">
                  {{ m.leido ? 'Leído' : 'Nuevo' }}
                </span>
              </div>
              <p class="msg-item__body">{{ m.cuerpo }}</p>
              <span class="msg-item__meta">{{ m.creadoEn | date: 'dd/MM/yyyy HH:mm' }}</span>
            </li>
          }
        </ul>
      }
    </section>
  `,
})
export class MensajesPageContainer implements OnInit {
  private readonly http = inject(HttpClient);
  readonly auth = inject(AuthService);

  readonly etiquetaContacto = etiquetaContacto;
  readonly recibidos = signal<Mensaje[]>([]);
  readonly notificaciones = signal<Notificacion[]>([]);
  readonly contactos = signal<ContactoMensajeria[]>([]);
  readonly cargandoContactos = signal(false);
  readonly err = signal('');

  destId: number | null = null;
  asunto = '';
  cuerpo = '';

  ngOnInit(): void {
    void this.reload();
    void this.loadContactos();
  }

  async loadContactos(): Promise<void> {
    this.cargandoContactos.set(true);
    try {
      if (this.auth.isAdmin()) {
        const users = await firstValueFrom(this.http.get<Usuario[]>('/auth/usuarios'));
        const me = this.auth.user()?.userId;
        this.contactos.set(
          users
            .filter((u) => u.idUsuario !== me && u.tipo !== 'admin')
            .map((u) => ({
              usuarioId: u.idUsuario,
              nombre: u.nombre,
              apellido: u.apellido,
              email: u.email,
              tipo: u.tipo,
              contexto: u.tipo,
            })),
        );
      } else if (this.auth.isProfesor()) {
        this.contactos.set(await firstValueFrom(this.http.get<ContactoMensajeria[]>('/api/docentes/contactos-mensajeria')));
      } else if (this.auth.isApoderado()) {
        this.contactos.set(await firstValueFrom(this.http.get<ContactoMensajeria[]>('/api/apoderados/contactos-mensajeria')));
      } else {
        this.contactos.set([]);
      }
    } catch {
      this.contactos.set([]);
    } finally {
      this.cargandoContactos.set(false);
    }
  }

  async reload(): Promise<void> {
    try {
      const [r, n] = await Promise.all([
        firstValueFrom(this.http.get<Mensaje[]>('/api/mensajes/recibidos')),
        firstValueFrom(this.http.get<Notificacion[]>('/api/mensajes/mis-notificaciones')),
      ]);
      this.recibidos.set(r);
      this.notificaciones.set(n);
    } catch {
      this.err.set('No se pudo cargar los mensajes.');
    }
  }

  async enviar(): Promise<void> {
    this.err.set('');
    try {
      await firstValueFrom(
        this.http.post('/api/mensajes/enviar', {
          destinatarioUsuarioId: this.destId,
          asunto: this.asunto || 'Mensaje',
          cuerpo: this.cuerpo,
        }),
      );
      this.asunto = '';
      this.cuerpo = '';
      await this.reload();
    } catch (e: unknown) {
      this.err.set(this.parseError(e));
    }
  }

  private parseError(e: unknown): string {
    if (e instanceof HttpErrorResponse && typeof e.error === 'string') return e.error;
    return 'No se pudo enviar el mensaje. Verifique que tiene permiso para contactar a esa persona.';
  }
}
