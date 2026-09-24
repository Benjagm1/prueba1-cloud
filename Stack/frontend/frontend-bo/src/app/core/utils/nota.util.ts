/** Valida y normaliza nota chilena 1,0 – 7,0 con un decimal. */
export function normalizarNotaChilena(valor: unknown): number | null {
  if (valor === null || valor === undefined || valor === '') return null;
  const n = Math.round(Number(valor) * 10) / 10;
  if (!Number.isFinite(n) || n < 1 || n > 7) {
    throw new Error('Cada nota debe estar entre 1,0 y 7,0 (un decimal).');
  }
  return n;
}
