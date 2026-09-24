import { Component, OnInit, inject, signal } from '@angular/core';
import { AlumnoApiService, AlumnoFicha, ApoderadoContacto } from '../../core/services/alumno-api.service';
import { PerfilDto } from '../../core/services/docente-api.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-alumno-mi-perfil',
  standalone: true,
  templateUrl: './alumno-mi-perfil.html',
  styleUrl: './alumno-mi-perfil.css',
})
export class AlumnoMiPerfil implements OnInit {
  private readonly api = inject(AlumnoApiService);

  readonly perfil = signal<PerfilDto | null>(null);
  readonly ficha = signal<AlumnoFicha | null>(null);
  readonly apoderado = signal<ApoderadoContacto | null>(null);
  readonly cargando = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    void this.cargar();
  }

  async cargar(): Promise<void> {
    this.cargando.set(true);
    this.error.set('');
    try {
      const [perfil, ficha, apoderado] = await Promise.all([
        firstValueFrom(this.api.perfil()),
        firstValueFrom(this.api.miFicha()),
        firstValueFrom(this.api.miApoderado()).catch(() => null),
      ]);
      this.perfil.set(perfil);
      this.ficha.set(ficha);
      this.apoderado.set(apoderado);
    } catch {
      this.error.set('No se pudo cargar su perfil.');
    } finally {
      this.cargando.set(false);
    }
  }
}
