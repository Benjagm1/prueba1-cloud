import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { AlumnoApiService } from '../../core/services/alumno-api.service';
import { ContactoMensajeria, MensajeDto, NotificacionDto } from '../../core/services/docente-api.service';

@Component({
  selector: 'app-alumno-mensajes',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './alumno-mensajes.html',
  styleUrl: './alumno-mensajes.css',
})
export class AlumnoMensajes implements OnInit {
  private readonly api = inject(AlumnoApiService);

  readonly recibidos = signal<MensajeDto[]>([]);
  readonly notificaciones = signal<NotificacionDto[]>([]);
  readonly contactos = signal<ContactoMensajeria[]>([]);
  readonly error = signal('');
  readonly exito = signal('');
  readonly enviando = signal(false);

  destId: number | null = null;
  asunto = '';
  cuerpo = '';

  ngOnInit(): void {
    void this.reload();
    void this.loadContactos();
  }

  etiquetaContacto(c: ContactoMensajeria): string {
    const nombre = `${c.nombre} ${c.apellido}`.trim();
    return c.contexto ? `${nombre} (${c.contexto})` : nombre;
  }

  etiquetaRemitente(m: MensajeDto): string {
    const nombre = `${m.remitenteNombre ?? ''} ${m.remitenteApellido ?? ''}`.trim();
    return nombre || 'Remitente';
  }

  async loadContactos(): Promise<void> {
    try {
      const lista = await firstValueFrom(this.api.contactosMensajeria());
      this.contactos.set(lista ?? []);
    } catch {
      this.contactos.set([]);
    }
  }

  async reload(): Promise<void> {
    this.error.set('');
    try {
      const [r, n] = await Promise.all([
        firstValueFrom(this.api.mensajesRecibidos()),
        firstValueFrom(this.api.misNotificaciones()),
      ]);
      this.recibidos.set(r);
      this.notificaciones.set(n);
    } catch {
      this.error.set('No se pudo cargar los mensajes.');
    }
  }

  async enviar(): Promise<void> {
    if (this.destId == null || !this.cuerpo.trim()) return;
    this.enviando.set(true);
    this.error.set('');
    this.exito.set('');
    try {
      await firstValueFrom(
        this.api.enviarMensaje({
          destinatarioUsuarioId: this.destId,
          asunto: this.asunto.trim() || 'Mensaje',
          cuerpo: this.cuerpo.trim(),
        }),
      );
      this.asunto = '';
      this.cuerpo = '';
      this.exito.set('Mensaje enviado correctamente.');
      await this.reload();
    } catch {
      this.error.set('No se pudo enviar el mensaje.');
    } finally {
      this.enviando.set(false);
    }
  }
}
