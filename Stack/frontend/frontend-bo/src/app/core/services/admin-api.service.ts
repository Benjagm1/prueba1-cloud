import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AsignacionDocenteResumen,
  Asignatura,
  AuditoriaEvento,
  Curso,
  FamiliaFormValue,
  ProvisionFormValue,
  Usuario,
} from '../models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminApiService {
  constructor(private readonly http: HttpClient) {}

  listarUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>('/auth/usuarios');
  }

  listarCursos(): Observable<Curso[]> {
    return this.http.get<Curso[]>('/api/admin/catalogo/cursos');
  }

  listarAsignaturas(): Observable<Asignatura[]> {
    return this.http.get<Asignatura[]>('/api/admin/catalogo/asignaturas');
  }

  asignacionesProfesor(profesorId: number): Observable<AsignacionDocenteResumen[]> {
    return this.http.get<AsignacionDocenteResumen[]>(`/api/admin/docentes/${profesorId}/asignaciones`);
  }

  agregarAsignacion(profesorUsuarioId: number, cursoId: number, asignaturaId: number): Observable<unknown> {
    return this.http.post('/api/admin/docentes/asignaciones', { profesorUsuarioId, cursoId, asignaturaId });
  }

  provisionarFamilia(v: FamiliaFormValue): Observable<{ mensaje?: string }> {
    return this.http.post<{ mensaje?: string }>('/api/admin/cuentas/provisionar-familia', {
      cursoId: v.cursoId,
      alumno: v.alumno,
      apoderado: v.apoderado,
    });
  }

  listarAuditoria(limite = 200): Observable<AuditoriaEvento[]> {
    return this.http.get<AuditoriaEvento[]>(`/api/admin/auditoria?limite=${limite}`);
  }

  provisionar(v: ProvisionFormValue): Observable<{ estadoCuenta?: { detalle?: string } }> {
    const body: Record<string, unknown> = {
      nombre: v.nombre.trim(),
      apellido: v.apellido.trim(),
      email: v.email.trim(),
      contrasena: v.contrasena,
      rol: v.rol,
    };
    if (v.rol === 'alumno') {
      body['cursoId'] = v.cursoId;
      body['apoderadoUsuarioId'] = v.apoderadoUsuarioId;
    } else if (v.rol === 'profesor') {
      body['cursoId'] = v.cursoId;
      body['asignaturaId'] = v.asignaturaId;
    }
    return this.http.post<{ estadoCuenta?: { detalle?: string } }>('/api/admin/cuentas/provisionar', body);
  }
}
