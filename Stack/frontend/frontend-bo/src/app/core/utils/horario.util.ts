import { AsignacionDocente } from './asignacion.util';

export type EstadoClase = 'completed' | 'active' | 'pending';

export interface BloqueHorario {
  inicio: string;
  fin: string;
  sala: string;
}

export interface ClaseHorario {
  inicio: string;
  fin: string;
  cursoNombre: string;
  asignaturaNombre: string;
  sala: string;
  cursoId: number;
  asignaturaId: number;
  diaSemana: number;
  estado: EstadoClase;
}

export const BLOQUES_LECTIVOS: BloqueHorario[] = [
  { inicio: '08:15', fin: '09:45', sala: 'Sala 12' },
  { inicio: '10:00', fin: '11:30', sala: 'Sala 14' },
  { inicio: '11:45', fin: '13:15', sala: 'Laboratorio 2' },
  { inicio: '14:30', fin: '16:00', sala: 'Sala 16' },
];

export const NOMBRES_DIA = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];

const DIAS_LECTIVOS = new Set([1, 2, 3, 4, 5]);

export function esDiaLectivo(fecha: Date = new Date()): boolean {
  return DIAS_LECTIVOS.has(fecha.getDay());
}

function parseHoraEnFecha(fecha: Date, hhmm: string): Date {
  const [h, m] = hhmm.split(':').map(Number);
  const d = new Date(fecha);
  d.setHours(h, m, 0, 0);
  return d;
}

export function calcularEstadoClase(inicio: string, fin: string, ahora: Date = new Date()): EstadoClase {
  if (!esDiaLectivo(ahora)) {
    return 'pending';
  }
  const t0 = parseHoraEnFecha(ahora, inicio).getTime();
  const t1 = parseHoraEnFecha(ahora, fin).getTime();
  const t = ahora.getTime();
  if (t >= t1) return 'completed';
  if (t >= t0) return 'active';
  return 'pending';
}

function asignacionesOrdenadas(asignaciones: AsignacionDocente[]): AsignacionDocente[] {
  return [...asignaciones].sort((a, b) => {
    const cmp = a.cursoNombre.localeCompare(b.cursoNombre, 'es', { sensitivity: 'base' });
    if (cmp !== 0) return cmp;
    return a.cursoId - b.cursoId;
  });
}

export function horarioDelDia(
  asignaciones: AsignacionDocente[],
  ahora: Date = new Date(),
  calcularEstado = true,
): ClaseHorario[] {
  if (!esDiaLectivo(ahora)) {
    return [];
  }
  const ordenadas = asignacionesOrdenadas(asignaciones);
  return ordenadas.slice(0, BLOQUES_LECTIVOS.length).map((asig, i) => {
    const bloque = BLOQUES_LECTIVOS[i];
    return {
      inicio: bloque.inicio,
      fin: bloque.fin,
      cursoNombre: asig.cursoNombre,
      asignaturaNombre: asig.asignaturaNombre,
      sala: bloque.sala,
      cursoId: asig.cursoId,
      asignaturaId: asig.asignaturaId,
      diaSemana: ahora.getDay(),
      estado: calcularEstado ? calcularEstadoClase(bloque.inicio, bloque.fin, ahora) : 'pending',
    };
  });
}

export function horarioSemanal(asignaciones: AsignacionDocente[], ahora: Date = new Date()): ClaseHorario[][] {
  const hoy = ahora.getDay();
  const filas: ClaseHorario[][] = [];
  for (let dia = 1; dia <= 5; dia++) {
    const fechaRef = new Date(ahora);
    const diff = dia - hoy;
    fechaRef.setDate(fechaRef.getDate() + diff);
    const esHoy = dia === hoy;
    filas.push(horarioDelDia(asignaciones, esHoy ? ahora : fechaRef, esHoy));
  }
  return filas;
}

export function proximaClaseHoy(clases: ClaseHorario[]): ClaseHorario | null {
  const activa = clases.find((c) => c.estado === 'active');
  if (activa) return activa;
  return clases.find((c) => c.estado === 'pending') ?? null;
}

export function etiquetaEstado(estado: EstadoClase): string {
  switch (estado) {
    case 'completed':
      return 'COMPLETADA';
    case 'active':
      return 'EN CURSO';
    default:
      return 'PENDIENTE';
  }
}

export function claseCssEstado(estado: EstadoClase): string {
  switch (estado) {
    case 'completed':
      return 'completed';
    case 'active':
      return 'active';
    default:
      return 'pending';
  }
}

export function badgeCssEstado(estado: EstadoClase): string {
  switch (estado) {
    case 'completed':
      return 'badge-gray';
    case 'active':
      return 'badge-dark';
    default:
      return 'badge-light';
  }
}
