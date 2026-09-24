import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AsignacionDocente } from '../utils/asignacion.util';

export interface AlumnoLista {
  alumnoUsuarioId: number;
  nombre: string;
  apellido: string;
}

export interface CalificacionDto {
  id: number;
  alumnoUsuarioId: number;
  cursoId: number;
  asignaturaId: number;
  nombreEvaluacion: string;
  nota: number;
}

export interface AnotacionDto {
  id: number;
  alumnoUsuarioId: number;
  profesorUsuarioId: number;
  profesorNombre?: string;
  asignaturaId?: number | null;
  asignaturaNombre?: string | null;
  texto: string;
  tipo: string;
  creadoEn: string;
}

export interface ContactoMensajeria {
  usuarioId: number;
  nombre: string;
  apellido: string;
  email: string;
  tipo: string;
  contexto?: string;
}

export interface MensajeDto {
  id: number;
  asunto: string;
  cuerpo: string;
  leido: boolean;
  creadoEn: string;
  remitenteUsuarioId?: number;
  remitenteNombre?: string;
  remitenteApellido?: string;
}

export interface NotificacionDto {
  id: number;
  titulo: string;
  cuerpo: string;
  leida: boolean;
  creadoEn: string;
}

export interface ResumenAsistenciaDto {
  diasRegistrados: number;
  diasPresentes: number;
  porcentaje: number;
  bajoUmbral85: boolean;
}

export interface ApoderadoContacto {
  userId: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono?: string;
}

export interface PerfilDto {
  idUsuario: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono?: string | null;
  tipo: string;
}

@Injectable({ providedIn: 'root' })
export class DocenteApiService {
  private readonly http = inject(HttpClient);

  misAsignaciones(): Observable<AsignacionDocente[]> {
    return this.http.get<AsignacionDocente[]>('/api/docentes/mis-asignaciones');
  }

  listaAlumnos(cursoId: number, asignaturaId: number): Observable<AlumnoLista[]> {
    return this.http.get<AlumnoLista[]>('/api/docentes/lista-alumnos', {
      params: { cursoId: String(cursoId), asignaturaId: String(asignaturaId) },
    });
  }

  notasAlumno(alumnoUsuarioId: number): Observable<CalificacionDto[]> {
    return this.http.get<CalificacionDto[]>(`/api/academico/alumnos/${alumnoUsuarioId}/notas`);
  }

  registrarNota(body: {
    alumnoUsuarioId: number;
    cursoId: number;
    asignaturaId: number;
    nombreEvaluacion: string;
    nota: number;
  }): Observable<CalificacionDto> {
    return this.http.post<CalificacionDto>('/api/academico/calificaciones', body);
  }

  anotacionesAlumno(alumnoUsuarioId: number): Observable<AnotacionDto[]> {
    return this.http.get<AnotacionDto[]>(`/api/conducta/alumnos/${alumnoUsuarioId}/anotaciones`);
  }

  crearAnotacion(body: {
    alumnoUsuarioId: number;
    tipo: string;
    texto: string;
    cursoId: number;
    asignaturaId: number;
  }): Observable<AnotacionDto> {
    return this.http.post<AnotacionDto>('/api/conducta/anotaciones', body);
  }

  contactosMensajeria(): Observable<ContactoMensajeria[]> {
    return this.http.get<ContactoMensajeria[]>('/api/docentes/contactos-mensajeria');
  }

  apoderadosDeAlumno(alumnoUsuarioId: number): Observable<ApoderadoContacto[]> {
    return this.http.get<ApoderadoContacto[]>(`/api/docentes/alumnos/${alumnoUsuarioId}/contacto-apoderados`);
  }

  mensajesRecibidos(): Observable<MensajeDto[]> {
    return this.http.get<MensajeDto[]>('/api/mensajes/recibidos');
  }

  resumenAsistenciaDocente(desde: string, hasta: string): Observable<ResumenAsistenciaDto> {
    return this.http.get<ResumenAsistenciaDto>('/api/asistencia/docente/resumen', {
      params: { desde, hasta },
    });
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
