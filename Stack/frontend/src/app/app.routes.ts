import { Routes } from '@angular/router';
import {
  adminGuard,
  alumnoGuard,
  apoderadoGuard,
  authGuard,
  guestGuard,
  profesorGuard,
} from './core/guards/auth.guard';
import { LoginPageContainer } from './features/login/containers/login-page.container';
import { LayoutComponent } from './features/shell/layout.component';
import { HomeComponent } from './features/home/home.component';
import { AdminPageContainer } from './features/admin/containers/admin-page.container';
import { ProfesorPageContainer } from './features/profesor/profesor-page.container';
import { ApoderadoPageContainer } from './features/apoderado/apoderado-page.container';
import { AlumnoPageContainer } from './features/alumno/alumno-page.container';
import { MensajesPageContainer } from './features/mensajes/containers/mensajes-page.container';
import { PerfilComponent } from './features/perfil/perfil.component';

export const routes: Routes = [
  { path: 'login', component: LoginPageContainer, canActivate: [guestGuard] },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: HomeComponent },
      { path: 'admin', component: AdminPageContainer, canActivate: [adminGuard] },
      { path: 'profesor', component: ProfesorPageContainer, canActivate: [profesorGuard] },
      { path: 'apoderado', component: ApoderadoPageContainer, canActivate: [apoderadoGuard] },
      { path: 'alumno', component: AlumnoPageContainer, canActivate: [alumnoGuard] },
      { path: 'mensajes', component: MensajesPageContainer },
      { path: 'perfil', component: PerfilComponent },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
