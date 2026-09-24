import { Asignatura, Curso, Usuario } from '../models/admin.model';

export function esCursoMedia(c: Curso): boolean {
  return c.codigo.startsWith('MED');
}

export function esCursoBasica(c: Curso): boolean {
  return c.codigo.startsWith('BAS');
}

export function nombreCompleto(u: Usuario): string {
  return `${u.nombre} ${u.apellido}`.trim();
}

export function etiquetaUsuario(u: Usuario): string {
  return `${nombreCompleto(u)} — ${u.email}`;
}

export function etiquetaCurso(c: Curso): string {
  return `${c.codigo} — ${c.nombre}`;
}

export function etiquetaAsignatura(a: Asignatura): string {
  return a.nombre;
}

export function rolLabel(tipo: string): string {
  const map: Record<string, string> = {
    alumno: 'Alumno',
    apoderado: 'Apoderado',
    profesor: 'Profesor',
    admin: 'Administrador',
  };
  return map[tipo] ?? tipo;
}

export function badgeClass(tipo: string): string {
  const map: Record<string, string> = {
    alumno: 'badge-alumno',
    apoderado: 'badge-apoderado',
    profesor: 'badge-profesor',
    admin: 'badge-admin',
  };
  return map[tipo] ?? 'badge-default';
}
