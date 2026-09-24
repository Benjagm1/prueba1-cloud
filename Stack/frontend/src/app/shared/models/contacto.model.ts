export interface ContactoMensajeria {
  usuarioId: number;
  nombre: string;
  apellido: string;
  email: string;
  tipo: string;
  contexto: string;
}

export function etiquetaContacto(c: ContactoMensajeria): string {
  return `${c.nombre} ${c.apellido} — ${c.contexto}`;
}
