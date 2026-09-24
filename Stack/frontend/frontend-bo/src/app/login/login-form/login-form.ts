import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

type PerfilLanding = 'alumno' | 'profesor' | 'apoderado';

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './login-form.html',
  styleUrl: './login-form.css',
})
export class LoginForm implements OnInit {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);

  email = '';
  contrasena = '';
  perfilEsperado: PerfilLanding | null = null;
  readonly error = signal('');
  readonly loading = signal(false);

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const perfil = params.get('perfil');
      if (perfil === 'alumno' || perfil === 'profesor' || perfil === 'apoderado') {
        this.perfilEsperado = perfil;
      }
      const err = params.get('error');
      if (err === 'solo-profesor') {
        this.error.set('Acceso restringido al portal de docentes.');
      } else if (err === 'solo-apoderado') {
        this.error.set('Acceso restringido al portal de apoderados.');
      } else if (err === 'solo-alumno') {
        this.error.set('Acceso restringido al portal de alumnos.');
      } else if (err === 'solo-admin') {
        this.error.set('Acceso restringido al portal de administración.');
      }
    });
  }

  async onLogin(event?: Event): Promise<void> {
    event?.preventDefault();
    this.error.set('');
    this.loading.set(true);
    try {
      await this.auth.login(this.email.trim(), this.contrasena);
      const tipo = this.auth.user()?.tipo;

      if (tipo === 'admin') {
        await this.router.navigate(['/admin']);
        return;
      }

      if (this.perfilEsperado === 'profesor' && tipo !== 'profesor') {
        this.error.set('Esta cuenta no corresponde al portal de docentes.');
        this.auth.logoutSilencioso();
        return;
      }
      if (this.perfilEsperado === 'alumno' && tipo !== 'alumno') {
        this.error.set('Esta cuenta no corresponde al portal de alumnos.');
        this.auth.logoutSilencioso();
        return;
      }
      if (this.perfilEsperado === 'apoderado' && tipo !== 'apoderado') {
        this.error.set('Esta cuenta no corresponde al portal de apoderados.');
        this.auth.logoutSilencioso();
        return;
      }

      if (tipo === 'profesor') {
        await this.router.navigate(['/profesor']);
      } else if (tipo === 'apoderado') {
        await this.router.navigate(['/apoderado']);
      } else if (tipo === 'alumno') {
        await this.router.navigate(['/alumno']);
      } else {
        this.error.set('Perfil no reconocido.');
        this.auth.logoutSilencioso();
      }
    } catch {
      this.error.set('No se pudo iniciar sesión. Verifique correo y contraseña.');
    } finally {
      this.loading.set(false);
    }
  }
}
