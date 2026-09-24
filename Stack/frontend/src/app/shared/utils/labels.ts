import { Asignatura, Curso, Usuario } from '../models/catalog.model';

export function nombreCompleto(u: Pick<Usuario, 'nombre' | 'apellido'>): string {
  return `${u.nombre} ${u.apellido}`.trim();
}

export function etiquetaUsuario(u: Usuario): string {
  return `${nombreCompleto(u)} — ${u.email}`;
}

export function etiquetaCurso(c: Curso): string {
  return `${c.nombre} (${c.codigo})`;
}

export function etiquetaAsignatura(a: Asignatura): string {
  return a.nombre;
}

export function rolLabel(tipo: string): string {
  const map: Record<string, string> = {
    admin: 'Administrador',
    alumno: 'Alumno',
    apoderado: 'Apoderado',
    profesor: 'Profesor',
  };
  return map[tipo?.toLowerCase()] ?? tipo;
}

export function badgeClass(tipo: string): string {
  const t = tipo?.toLowerCase();
  if (t === 'admin') return 'badge--admin';
  if (t === 'alumno') return 'badge--alumno';
  if (t === 'apoderado') return 'badge--apoderado';
  if (t === 'profesor') return 'badge--profesor';
  return '';
}
