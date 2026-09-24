package ms.administracion.web;

import ms.administracion.service.AdministracionDominioService;
import ms.common.security.MsUserPrincipal;
import ms.common.security.Roles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/alumnos")
public class AlumnoConsultasController {

	private final AdministracionDominioService dominio;

	public AlumnoConsultasController(AdministracionDominioService dominio) {
		this.dominio = dominio;
	}

	@GetMapping("/mi-ficha")
	public ResponseEntity<AdministracionDominioService.AlumnoFichaResumen> miFicha(
			@AuthenticationPrincipal MsUserPrincipal principal) {
		requerirAlumno(principal);
		return dominio.fichaAlumno(principal.userId())
				.map(ResponseEntity::ok)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no inscrito en un curso"));
	}

	@GetMapping("/contactos-mensajeria")
	public ResponseEntity<List<AdministracionDominioService.ContactoMensajeria>> contactosMensajeria(
			@AuthenticationPrincipal MsUserPrincipal principal) {
		requerirAlumno(principal);
		return ResponseEntity.ok(dominio.contactosMensajeriaAlumno(principal.userId()));
	}

	@GetMapping("/mi-apoderado")
	public ResponseEntity<AdministracionDominioService.ApoderadoContactoResumen> miApoderado(
			@AuthenticationPrincipal MsUserPrincipal principal) {
		requerirAlumno(principal);
		return dominio.apoderadoVinculadoAlumno(principal.userId())
				.map(ResponseEntity::ok)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sin apoderado vinculado"));
	}

	private static void requerirAlumno(MsUserPrincipal principal) {
		if (principal == null || !Roles.isAlumno(principal.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo alumnos");
		}
	}
}
