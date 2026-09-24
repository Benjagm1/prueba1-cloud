import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { AlumnoApiService } from '../../core/services/alumno-api.service';
import { AnotacionDto } from '../../core/services/docente-api.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-alumno-anotaciones',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './alumno-anotaciones.html',
  styleUrl: './alumno-anotaciones.css',
})
export class AlumnoAnotaciones implements OnInit {
  private readonly api = inject(AlumnoApiService);
  private readonly auth = inject(AuthService);

  readonly anotaciones = signal<AnotacionDto[]>([]);
  readonly cargando = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    void this.cargar();
  }

  async cargar(): Promise<void> {
    const userId = this.auth.user()?.userId;
    if (!userId) {
      this.error.set('Sesión no válida.');
      this.cargando.set(false);
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    try {
      const lista = await firstValueFrom(this.api.misAnotaciones(userId));
      this.anotaciones.set(lista ?? []);
    } catch {
      this.error.set('No se pudieron cargar sus anotaciones.');
      this.anotaciones.set([]);
    } finally {
      this.cargando.set(false);
    }
  }

  etiquetaTipo(tipo: string): string {
    return tipo === 'negativa' ? 'Observación' : 'Reconocimiento';
  }
}
