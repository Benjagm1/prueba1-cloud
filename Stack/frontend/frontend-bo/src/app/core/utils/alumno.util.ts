export interface AlumnoNombreApellido {
  nombre: string;
  apellido: string;
}

export function sortAlumnosPorApellidoNombre<T extends AlumnoNombreApellido>(lista: T[]): T[] {
  return [...lista].sort((a, b) => {
    const cmpAp = a.apellido.localeCompare(b.apellido, 'es', { sensitivity: 'base' });
    if (cmpAp !== 0) return cmpAp;
    return a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' });
  });
}

export function nombreCompletoAlumno(alumno: AlumnoNombreApellido): string {
  return `${alumno.nombre} ${alumno.apellido}`.trim();
}
