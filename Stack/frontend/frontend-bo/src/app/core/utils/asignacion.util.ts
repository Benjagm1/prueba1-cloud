export interface AsignacionDocente {
  cursoId: number;
  cursoNombre: string;
  asignaturaId: number;
  asignaturaNombre: string;
}

export interface CursoCard {
  grado: string;
  asignatura: string;
  alumnos: string;
  badge: string;
  cursoId: number;
  asignaturaId: number;
}

export function parseAsignacionKey(key: string): { cursoId: number; asignaturaId: number } | null {
  if (!key.includes('-')) return null;
  const [c, a] = key.split('-').map(Number);
  if (!Number.isFinite(c) || !Number.isFinite(a)) return null;
  return { cursoId: c, asignaturaId: a };
}

export function toAsignacionKey(cursoId: number, asignaturaId: number): string {
  return `${cursoId}-${asignaturaId}`;
}

export function mapAsignaciones(data: AsignacionDocente[]): CursoCard[] {
  return data.map((a) => ({
    grado: a.cursoNombre,
    asignatura: a.asignaturaNombre,
    alumnos: '—',
    badge: toAsignacionKey(a.cursoId, a.asignaturaId),
    cursoId: a.cursoId,
    asignaturaId: a.asignaturaId,
  }));
}
