import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { LOGO_URL } from '../../../shared/branding';
import { AuthService } from '../../../core/services/auth.service';
import { LoginCredentials, LoginFormComponent } from '../presentational/login-form.component';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [LoginFormComponent],
  template: `
    <div class="login-root">
      <aside class="login-left">
        <img class="login-brand-logo" [src]="logoUrl" alt="Colegio Bernardo O'Higgins" />
        <h1>Colegio Bernardo O'Higgins</h1>
        <p>Plataforma digital para notas, asistencia y comunicación entre la comunidad escolar.</p>
      </aside>
      <div class="login-right">
        <app-login-form [error]="error()" [loading]="loading()" (loginSubmit)="onLogin($event)" />
      </div>
    </div>
  `,
})
export class LoginPageContainer {
  readonly logoUrl = LOGO_URL;
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly error = signal('');
  readonly loading = signal(false);

  async onLogin(creds: LoginCredentials): Promise<void> {
    this.error.set('');
    this.loading.set(true);
    try {
      await this.auth.login(creds.email, creds.contrasena);
      await this.router.navigateByUrl('/');
    } catch (e: unknown) {
      this.error.set(this.parseError(e));
    } finally {
      this.loading.set(false);
    }
  }

  private parseError(e: unknown): string {
    if (e && typeof e === 'object' && 'error' in e) {
      const err = (e as { error: unknown }).error;
      if (typeof err === 'string') return err;
      if (err && typeof err === 'object' && 'message' in err) {
        return String((e as { error: { message: string } }).error.message);
      }
    }
    return 'No se pudo iniciar sesión. Verifique su correo y contraseña.';
  }
}
