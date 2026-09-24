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
} from './docente-api.service';
import { AlumnoFicha, RegistroAsistenciaDto } from './alumno-api.service';

export interface PupiloResumen {
  alumnoUsuarioId: number;
  nombre: string;
  apellido: string;
}

@Injectable({ providedIn: 'root' })
export class ApoderadoApiService {
  private readonly http = inject(HttpClient);

  misPupilos(): Observable<PupiloResumen[]> {
    return this.http.get<PupiloResumen[]>('/api/apoderados/mis-pupilos');
  }

  fichaPupilo(alumnoUsuarioId: number): Observable<AlumnoFicha> {
    return this.http.get<AlumnoFicha>(`/api/apoderados/pupilos/${alumnoUsuarioId}/ficha`);
  }

  notasPupilo(alumnoUsuarioId: number): Observable<CalificacionDto[]> {
    return this.http.get<CalificacionDto[]>(`/api/academico/alumnos/${alumnoUsuarioId}/notas`);
  }

  historialPupilo(alumnoUsuarioId: number, desde: string, hasta: string): Observable<RegistroAsistenciaDto[]> {
    return this.http.get<RegistroAsistenciaDto[]>(`/api/asistencia/alumnos/${alumnoUsuarioId}/historial`, {
      params: { desde, hasta },
    });
  }

  anotacionesPupilo(alumnoUsuarioId: number): Observable<AnotacionDto[]> {
    return this.http.get<AnotacionDto[]>(`/api/conducta/alumnos/${alumnoUsuarioId}/anotaciones`);
  }

  actualizarPupilo(alumnoUsuarioId: number, nombre: string, apellido: string): Observable<void> {
    return this.http.put<void>(`/api/apoderados/pupilos/${alumnoUsuarioId}/datos-personales`, { nombre, apellido });
  }

  actualizarMiPerfil(body: {
    nombre?: string;
    apellido?: string;
    email?: string;
    telefono?: string;
  }): Observable<string> {
    return this.http.put('/auth/perfil', body, { responseType: 'text' });
  }

  contactosMensajeria(): Observable<ContactoMensajeria[]> {
    return this.http.get<ContactoMensajeria[]>('/api/apoderados/contactos-mensajeria');
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
}
