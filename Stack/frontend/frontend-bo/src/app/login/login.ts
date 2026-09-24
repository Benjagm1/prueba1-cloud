import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

  irLogin(perfil: 'alumno' | 'profesor' | 'apoderado'): void {
    this.auth.logoutSilencioso();
    void this.router.navigate(['/login/form'], { queryParams: { perfil } });
  }
}
