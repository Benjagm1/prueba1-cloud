import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import {
  DocenteApiService,
  MensajeDto,
  NotificacionDto,
  ApoderadoContacto,
  AlumnoLista,
} from '../../core/services/docente-api.service';
import { AsignacionDocente } from '../../core/utils/asignacion.util';
import { nombreCompletoAlumno, sortAlumnosPorApellidoNombre } from '../../core/utils/alumno.util';

@Component({
  selector: 'app-profesor-mensajes',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './profesor-mensajes.html',
  styleUrl: './profesor-mensajes.css',
})
export class ProfesorMensajes implements OnInit {
  private readonly api = inject(DocenteApiService);

  readonly recibidos = signal<MensajeDto[]>([]);
  readonly notificaciones = signal<NotificacionDto[]>([]);
  readonly cursos = signal<AsignacionDocente[]>([]);
  readonly alumnos = signal<AlumnoLista[]>([]);
  readonly apoderados = signal<ApoderadoContacto[]>([]);
  readonly error = signal('');
  readonly enviando = signal(false);
  readonly exito = signal('');
  readonly cargandoAlumnos = signal(false);
  readonly cargandoApoderados = signal(false);

  cursoKey: string | null = null;
  alumnoId: number | null = null;
  destId: number | null = null;
  asunto = '';
  cuerpo = '';

  ngOnInit(): void {
    void this.reload();
    void this.loadCursos();
  }

  etiquetaRemitente(m: MensajeDto): string {
    const nombre = `${m.remitenteNombre ?? ''} ${m.remitenteApellido ?? ''}`.trim();
    return nombre || 'Apoderado';
  }

  etiquetaAlumno(a: AlumnoLista): string {
    return nombreCompletoAlumno(a);
  }

  etiquetaApoderado(a: ApoderadoContacto): string {
    return `${a.nombre} ${a.apellido}`.trim();
  }

  async loadCursos(): Promise<void> {
    try {
      const asignaciones = await firstValueFrom(this.api.misAsignaciones());
      const vistos = new Set<number>();
      const unicos: AsignacionDocente[] = [];
      for (const a of asignaciones) {
        if (!vistos.has(a.cursoId)) {
          vistos.add(a.cursoId);
          unicos.push(a);
        }
      }
      unicos.sort((x, y) => x.cursoNombre.localeCompare(y.cursoNombre, 'es', { sensitivity: 'base' }));
      this.cursos.set(unicos);
    } catch {
      this.cursos.set([]);
    }
  }

  async onCursoChange(): Promise<void> {
    this.alumnoId = null;
    this.destId = null;
    this.alumnos.set([]);
    this.apoderados.set([]);
    if (!this.cursoKey) return;

    const [cursoId, asignaturaId] = this.cursoKey.split('-').map(Number);
    this.cargandoAlumnos.set(true);
    try {
      const lista = await firstValueFrom(this.api.listaAlumnos(cursoId, asignaturaId));
      this.alumnos.set(sortAlumnosPorApellidoNombre(lista));
    } catch {
      this.alumnos.set([]);
      this.error.set('No se pudo cargar los alumnos del curso.');
    } finally {
      this.cargandoAlumnos.set(false);
    }
  }

  async onAlumnoChange(): Promise<void> {
    this.destId = null;
    this.apoderados.set([]);
    if (this.alumnoId == null) return;

    this.cargandoApoderados.set(true);
    try {
      const lista = await firstValueFrom(this.api.apoderadosDeAlumno(this.alumnoId));
      this.apoderados.set(lista);
      if (lista.length === 1) {
        this.destId = lista[0].userId;
      }
    } catch {
      this.apoderados.set([]);
      this.error.set('No se pudo obtener el apoderado del alumno.');
    } finally {
      this.cargandoApoderados.set(false);
    }
  }

  cursoOptionValue(c: AsignacionDocente): string {
    return `${c.cursoId}-${c.asignaturaId}`;
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
      this.exito.set('Mensaje enviado correctamente al apoderado.');
      await this.reload();
    } catch {
      this.error.set('No se pudo enviar el mensaje.');
    } finally {
      this.enviando.set(false);
    }
  }
}
