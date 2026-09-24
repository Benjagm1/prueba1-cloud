import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="card">
      <h2>Mi perfil</h2>
      <div class="form-grid">
        <label>email <input type="email" [(ngModel)]="email" name="email" /></label>
        <label>teléfono <input [(ngModel)]="telefono" name="tel" /></label>
        <button type="button" class="btn btn-primary" (click)="guardar()">Guardar</button>
      </div>
      @if (msg()) {
        <div class="alert alert-ok">{{ msg() }}</div>
      }
    </div>
  `,
})
export class PerfilComponent {
  private readonly http = inject(HttpClient);
  email = '';
  telefono = '';
  readonly msg = signal('');

  async guardar(): Promise<void> {
    await firstValueFrom(
      this.http.put('/auth/perfil', { email: this.email || null, telefono: this.telefono || null }),
    );
    this.msg.set('Datos actualizados');
  }
}
