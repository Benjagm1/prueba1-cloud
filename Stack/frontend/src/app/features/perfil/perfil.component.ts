import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { PageHeaderComponent } from '../../shared/components/page-header.component';
import { AuthService } from '../../core/services/auth.service';

interface MiPerfil {
  idUsuario: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono?: string;
  tipo: string;
}

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [FormsModule, PageHeaderComponent],
  template: `
    <app-page-header
      title="Mi perfil"
      subtitle="Mantenga actualizados su correo y teléfono de contacto."
    />

    @if (msg()) {
      <div class="alert alert-ok" role="status">{{ msg() }}</div>
    }
    @if (err()) {
      <div class="alert alert-error" role="alert">{{ err() }}</div>
    }

    <section class="card" style="max-width: 520px">
      <div class="form-grid">
        <div class="field">
          <label for="email">Correo electrónico</label>
          <input id="email" type="email" [(ngModel)]="email" name="email" autocomplete="email" />
        </div>
        <div class="field">
          <label for="tel">Teléfono</label>
          <input id="tel" type="tel" [(ngModel)]="telefono" name="tel" autocomplete="tel" placeholder="+56 9 …" />
        </div>
      </div>
      <div class="form-actions">
        <button type="button" class="btn btn-primary" (click)="guardar()" [disabled]="saving()">
          {{ saving() ? 'Guardando…' : 'Guardar cambios' }}
        </button>
      </div>
    </section>
  `,
})
export class PerfilComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  email = this.auth.user()?.email ?? '';
  telefono = '';
  readonly msg = signal('');
  readonly err = signal('');
  readonly saving = signal(false);

  ngOnInit(): void {
    void this.cargar();
  }

  async cargar(): Promise<void> {
    try {
      const p = await firstValueFrom(this.http.get<MiPerfil>('/auth/perfil'));
      this.email = p.email ?? this.email;
      this.telefono = p.telefono ?? '';
    } catch {
      /* mantiene valores de sesión */
    }
  }

  async guardar(): Promise<void> {
    this.msg.set('');
    this.err.set('');
    this.saving.set(true);
    try {
      await firstValueFrom(
        this.http.put('/auth/perfil', { email: this.email || null, telefono: this.telefono || null }),
      );
      this.msg.set('Sus datos se actualizaron correctamente.');
      await this.cargar();
    } catch {
      this.err.set('No se pudieron guardar los cambios.');
    } finally {
      this.saving.set(false);
    }
  }
}
