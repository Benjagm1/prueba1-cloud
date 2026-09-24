import { Routes } from '@angular/router';
import { Login } from './login/login';
import { LoginForm } from './login/login-form/login-form';
import { NuestroColegio } from './nuestro-colegio/nuestro-colegio';
import { Profesor } from './profesor/profesor';
import { AsistenciaRegistro } from './profesor/asistencia-registro/asistencia-registro';
import { AsistenciaCursos } from './profesor/asistencia-cursos/asistencia-cursos';
import { ProfesorInicio } from './profesor/profesor-inicio/profesor-inicio';
import { AnotacionesCursos } from './profesor/anotaciones-cursos/anotaciones-cursos';
import { AnotacionesRegistro } from './profesor/anotaciones-registro/anotaciones-registro';
import { CalificacionesRegistro } from './profesor/calificaciones-registro/calificaciones-registro';
import { CalificacionesCursos } from './profesor/calificaciones-cursos/calificaciones-cursos';
import { ProfesorMensajes } from './profesor/mensajes/profesor-mensajes';
import { ProfesorHorario } from './profesor/profesor-horario/profesor-horario';
import { Apoderado } from './apoderado/apoderado';
import { ApoderadoInicio } from './apoderado/apoderado-inicio/apoderado-inicio';
import { ApoderadoMensajes } from './apoderado/apoderado-mensajes/apoderado-mensajes';
import { ApoderadoAsistencia } from './apoderado/apoderado-asistencia/apoderado-asistencia';
import { ApoderadoCalificaciones } from './apoderado/apoderado-calificaciones/apoderado-calificaciones';
import { ApoderadoMisDatos } from './apoderado/apoderado-mis-datos/apoderado-mis-datos';
import { ApoderadoAnotaciones } from './apoderado/apoderado-anotaciones/apoderado-anotaciones';
import { Alumno } from './alumno/alumno';
import { AlumnoInicio } from './alumno/alumno-inicio/alumno-inicio';
import { AlumnoAsistencia } from './alumno/alumno-asistencia/alumno-asistencia';
import { AlumnoCalificaciones } from './alumno/alumno-calificaciones/alumno-calificaciones';
import { AlumnoAnotaciones } from './alumno/alumno-anotaciones/alumno-anotaciones';
import { AlumnoMensajes } from './alumno/alumno-mensajes/alumno-mensajes';
import { AlumnoMiPerfil } from './alumno/alumno-mi-perfil/alumno-mi-perfil';
import { Admin } from './admin/admin';
import { authGuard, adminGuard, alumnoGuard, apoderadoGuard, loginFormGuard, profesorGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'login/form', component: LoginForm, canActivate: [loginFormGuard] },
  { path: 'nuestro-colegio', component: NuestroColegio },
  {
    path: 'profesor',
    component: Profesor,
    canActivate: [authGuard, profesorGuard],
    children: [
      { path: '', component: ProfesorInicio },
      { path: 'asistencia-cursos', component: AsistenciaCursos },
      { path: 'asistencia-registro/:idCurso', component: AsistenciaRegistro },
      { path: 'anotaciones', component: AnotacionesCursos },
      { path: 'anotaciones/registro/:idCurso', component: AnotacionesRegistro },
      { path: 'calificaciones', component: CalificacionesCursos },
      { path: 'calificaciones/registro/:idCurso', component: CalificacionesRegistro },
      { path: 'mensajes', component: ProfesorMensajes },
      { path: 'horario', component: ProfesorHorario },
    ],
  },
  {
    path: 'apoderado',
    component: Apoderado,
    canActivate: [authGuard, apoderadoGuard],
    children: [
      { path: '', component: ApoderadoInicio },
      { path: 'asistencia', component: ApoderadoAsistencia },
      { path: 'calificaciones', component: ApoderadoCalificaciones },
      { path: 'anotaciones', component: ApoderadoAnotaciones },
      { path: 'mis-datos', component: ApoderadoMisDatos },
      { path: 'mensajes', component: ApoderadoMensajes },
    ],
  },
  {
    path: 'alumno',
    component: Alumno,
    canActivate: [authGuard, alumnoGuard],
    children: [
      { path: '', component: AlumnoInicio },
      { path: 'asistencia', component: AlumnoAsistencia },
      { path: 'calificaciones', component: AlumnoCalificaciones },
      { path: 'anotaciones', component: AlumnoAnotaciones },
      { path: 'mensajes', component: AlumnoMensajes },
      { path: 'mi-perfil', component: AlumnoMiPerfil },
    ],
  },
  {
    path: 'admin',
    component: Admin,
    canActivate: [authGuard, adminGuard],
  },
];
