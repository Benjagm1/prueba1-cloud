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
        <p>Libro de clases digital: notas, asistencia y mensajes.</p>
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
      const msg =
        e && typeof e === 'object' && 'error' in e
          ? (typeof (e as { error: unknown }).error === 'string'
              ? (e as { error: string }).error
              : JSON.stringify((e as { error: unknown }).error))
          : 'No se pudo iniciar sesión';
      this.error.set(msg);
    } finally {
      this.loading.set(false);
    }
  }
}
