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
  idUsuario: number;
  nombre: string;
  apellido: string;
  email: string;
  tipo: string;
  telefono?: string;
}

export interface Calificacion {
  id: number;
  alumnoUsuarioId: number;
  cursoId: number;
  asignaturaId: number;
  nombreEvaluacion: string;
  nota: number;
  creadoEn?: string;
}

export interface Mensaje {
  id: number;
  remitenteUsuarioId: number;
  destinatarioUsuarioId: number;
  asunto: string;
  cuerpo: string;
  leido: boolean;
  creadoEn: string;
}

export interface Notificacion {
  id: number;
  titulo: string;
  cuerpo: string;
  leida: boolean;
  creadoEn: string;
}
