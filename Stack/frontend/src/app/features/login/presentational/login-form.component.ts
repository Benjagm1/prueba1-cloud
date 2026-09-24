import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

export interface LoginCredentials {
  email: string;
  contrasena: string;
}

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [FormsModule],
  template: `
    <form (ngSubmit)="submit()" class="login-card">
      <h2>Iniciar sesión</h2>
      <p class="subtitle">Acceso reservado a cuentas habilitadas por administración escolar.</p>
      @if (error) {
        <div class="alert alert-error" role="alert">{{ error }}</div>
      }
      <div class="field">
        <label for="email">Correo electrónico</label>
        <input
          id="email"
          type="email"
          [(ngModel)]="email"
          name="email"
          required
          autocomplete="username"
          placeholder="nombre@ejemplo.cl"
        />
      </div>
      <div class="field">
        <label for="pass">Contraseña</label>
        <input
          id="pass"
          type="password"
          [(ngModel)]="contrasena"
          name="pass"
          required
          autocomplete="current-password"
        />
      </div>
      <button class="btn btn-primary" type="submit" [disabled]="loading">
        {{ loading ? 'Ingresando…' : 'Ingresar' }}
      </button>
      <p class="login-footer">Si no tiene cuenta, solicítela a la secretaría del colegio.</p>
    </form>
  `,
})
export class LoginFormComponent {
  @Input() error = '';
  @Input() loading = false;
  @Output() readonly loginSubmit = new EventEmitter<LoginCredentials>();

  email = '';
  contrasena = '';

  submit(): void {
    this.loginSubmit.emit({ email: this.email.trim(), contrasena: this.contrasena });
  }
}
