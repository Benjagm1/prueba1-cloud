import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApoderadoApiService, PupiloResumen } from '../../core/services/apoderado-api.service';
import { PerfilDto } from '../../core/services/docente-api.service';
import { nombreCompletoPupilo } from '../../core/utils/pupilo.util';

@Component({
  selector: 'app-apoderado-mis-datos',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './apoderado-mis-datos.html',
  styleUrl: './apoderado-mis-datos.css',
})
export class ApoderadoMisDatos implements OnInit {
  private readonly api = inject(ApoderadoApiService);

  readonly pupilos = signal<PupiloResumen[]>([]);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly mensaje = signal('');
  readonly error = signal('');

  miNombre = '';
  miApellido = '';
  miEmail = '';
  miTelefono = '';

  pupiloId: number | null = null;
  pupiloNombre = '';
  pupiloApellido = '';

  ngOnInit(): void {
    void this.cargar();
  }

  etiquetaPupilo(p: PupiloResumen): string {
    return nombreCompletoPupilo(p);
  }

  async cargar(): Promise<void> {
    this.cargando.set(true);
    this.error.set('');
    try {
      const [perfil, lista] = await Promise.all([
        firstValueFrom(this.api.perfil()),
        firstValueFrom(this.api.misPupilos()),
      ]);
      this.aplicarPerfil(perfil);
      this.pupilos.set(lista ?? []);
      if (lista.length === 1) {
        this.pupiloId = lista[0].alumnoUsuarioId;
        await this.cargarPupilo();
      }
    } catch {
      this.error.set('No se pudieron cargar sus datos.');
    } finally {
      this.cargando.set(false);
    }
  }

  private aplicarPerfil(p: PerfilDto): void {
    this.miNombre = p.nombre ?? '';
    this.miApellido = p.apellido ?? '';
    this.miEmail = p.email ?? '';
    this.miTelefono = p.telefono ?? '';
  }

  async onPupiloChange(): Promise<void> {
    await this.cargarPupilo();
  }

  async cargarPupilo(): Promise<void> {
    if (this.pupiloId == null) {
      this.pupiloNombre = '';
      this.pupiloApellido = '';
      return;
    }
    try {
      const ficha = await firstValueFrom(this.api.fichaPupilo(this.pupiloId));
      this.pupiloNombre = ficha.nombre;
      this.pupiloApellido = ficha.apellido;
    } catch {
      this.error.set('No se pudieron cargar los datos del pupilo.');
    }
  }

  async guardarMiPerfil(): Promise<void> {
    this.guardando.set(true);
    this.mensaje.set('');
    this.error.set('');
    try {
      await firstValueFrom(
        this.api.actualizarMiPerfil({
          nombre: this.miNombre.trim(),
          apellido: this.miApellido.trim(),
          email: this.miEmail.trim(),
          telefono: this.miTelefono.trim() || undefined,
        }),
      );
      this.mensaje.set('Sus datos personales fueron actualizados.');
    } catch {
      this.error.set('No se pudieron guardar sus datos.');
    } finally {
      this.guardando.set(false);
    }
  }

  async guardarPupilo(): Promise<void> {
    if (this.pupiloId == null) return;
    this.guardando.set(true);
    this.mensaje.set('');
    this.error.set('');
    try {
      await firstValueFrom(
        this.api.actualizarPupilo(this.pupiloId, this.pupiloNombre.trim(), this.pupiloApellido.trim()),
      );
      this.mensaje.set('Los datos del pupilo fueron actualizados.');
      const lista = this.pupilos().map((p) =>
        p.alumnoUsuarioId === this.pupiloId
          ? { ...p, nombre: this.pupiloNombre.trim(), apellido: this.pupiloApellido.trim() }
          : p,
      );
      this.pupilos.set(lista);
    } catch {
      this.error.set('No se pudieron guardar los datos del pupilo.');
    } finally {
      this.guardando.set(false);
    }
  }
}
