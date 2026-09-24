export interface PupiloNombre {
  alumnoUsuarioId: number;
  nombre: string;
  apellido: string;
}

export function nombreCompletoPupilo(p: PupiloNombre): string {
  return `${p.nombre} ${p.apellido}`.trim();
}

export function rangoMesActual(): { desde: string; hasta: string } {
  const hoy = new Date();
  const desde = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
  const hasta = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
  const fmt = (d: Date) => d.toISOString().slice(0, 10);
  return { desde: fmt(desde), hasta: fmt(hasta) };
}
