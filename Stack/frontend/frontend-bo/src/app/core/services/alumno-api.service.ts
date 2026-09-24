import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AnotacionDto,
  CalificacionDto,
  ContactoMensajeria,
  MensajeDto,
  NotificacionDto,
  PerfilDto,
  ResumenAsistenciaDto,
} from './docente-api.service';

export interface AsignaturaResumen {
  id: number;
  nombre: string;
  codigo: string;
}

export interface AlumnoFicha {
  alumnoUsuarioId: number;
  nombre: string;
  apellido: string;
  cursoId: number;
  cursoNombre: string;
  cursoCodigo: string;
  asignaturas: AsignaturaResumen[];
}

export interface RegistroAsistenciaDto {
  id: number;
  fecha: string;
  presente: boolean;
  asignaturaId: number;
  asignaturaNombre: string;
}

export interface ApoderadoContacto {
  apoderadoUsuarioId: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono?: string | null;
}

@Injectable({ providedIn: 'root' })
export class AlumnoApiService {
  private readonly http = inject(HttpClient);

  miFicha(): Observable<AlumnoFicha> {
    return this.http.get<AlumnoFicha>('/api/alumnos/mi-ficha');
  }

  misNotas(alumnoUsuarioId: number): Observable<CalificacionDto[]> {
    return this.http.get<CalificacionDto[]>(`/api/academico/alumnos/${alumnoUsuarioId}/notas`);
  }

  miHistorialAsistencia(alumnoUsuarioId: number, desde: string, hasta: string): Observable<RegistroAsistenciaDto[]> {
    return this.http.get<RegistroAsistenciaDto[]>(`/api/asistencia/alumnos/${alumnoUsuarioId}/historial`, {
      params: { desde, hasta },
    });
  }

  miResumenAsistencia(desde: string, hasta: string): Observable<ResumenAsistenciaDto> {
    return this.http.get<ResumenAsistenciaDto>('/api/asistencia/mi-resumen', {
      params: { desde, hasta },
    });
  }

  misAnotaciones(alumnoUsuarioId: number): Observable<AnotacionDto[]> {
    return this.http.get<AnotacionDto[]>(`/api/conducta/alumnos/${alumnoUsuarioId}/anotaciones`);
  }

  contactosMensajeria(): Observable<ContactoMensajeria[]> {
    return this.http.get<ContactoMensajeria[]>('/api/alumnos/contactos-mensajeria');
  }

  mensajesRecibidos(): Observable<MensajeDto[]> {
    return this.http.get<MensajeDto[]>('/api/mensajes/recibidos');
  }

  misNotificaciones(): Observable<NotificacionDto[]> {
    return this.http.get<NotificacionDto[]>('/api/mensajes/mis-notificaciones');
  }

  enviarMensaje(body: { destinatarioUsuarioId: number; asunto: string; cuerpo: string }): Observable<void> {
    return this.http.post<void>('/api/mensajes/enviar', body);
  }

  perfil(): Observable<PerfilDto> {
    return this.http.get<PerfilDto>('/auth/perfil');
  }

  miApoderado(): Observable<ApoderadoContacto> {
    return this.http.get<ApoderadoContacto>('/api/alumnos/mi-apoderado');
  }
}
