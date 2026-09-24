export interface Curso {
  id: number;
  codigo: string;
  nombre: string;
  orden: number;
}

export interface Asignatura {
  id: number;
  nombre: string;
}

export interface Usuario {
  /** Auth API serializa el id como `id` (no idUsuario). */
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  tipo: string;
  telefono?: string;
}

export interface AuditoriaEvento {
  id: number;
  modulo: string;
  accion: string;
  actorUsuarioId: number | null;
  actorTipo: string | null;
  recursoId: number | null;
  detalle: string | null;
  creadoEn: string;
}

export interface AsignacionDocenteResumen {
  cursoId: number;
  cursoNombre: string;
  asignaturaId: number;
  asignaturaNombre: string;
}

export interface FamiliaFormValue {
  alumno: { nombre: string; apellido: string; email: string; contrasena: string };
  apoderado: { nombre: string; apellido: string; email: string; contrasena: string };
  cursoId: number | null;
}

export interface ProvisionFormValue {
  nombre: string;
  apellido: string;
  email: string;
  contrasena: string;
  rol: 'alumno' | 'profesor';
  cursoId: number | null;
  apoderadoUsuarioId: number | null;
  alumnoUsuarioId: number | null;
  asignaturaId: number | null;
}
