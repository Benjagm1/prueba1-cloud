package ms.administracion.web;

import ms.administracion.service.AdministracionDominioService;
import ms.common.security.MsUserPrincipal;
import ms.common.security.Roles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/apoderados")
public class ApoderadoPupilosController {

	private final AdministracionDominioService dominio;

	public ApoderadoPupilosController(AdministracionDominioService dominio) {
		this.dominio = dominio;
	}

	public record ActualizarDatosPupiloRequest(String nombre, String apellido) {
	}

	@GetMapping("/contactos-mensajeria")
	public ResponseEntity<List<AdministracionDominioService.ContactoMensajeria>> contactosMensajeria(
			@AuthenticationPrincipal MsUserPrincipal principal) {
		if (principal == null || !Roles.isApoderado(principal.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo apoderados");
		}
		return ResponseEntity.ok(dominio.contactosMensajeriaApoderado(principal.userId()));
	}

	@GetMapping("/mis-pupilos")
	public ResponseEntity<List<AdministracionDominioService.PupiloResumen>> misPupilos(
			@AuthenticationPrincipal MsUserPrincipal principal) {
		if (principal == null || !Roles.isApoderado(principal.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo apoderados");
		}
		return ResponseEntity.ok(dominio.listarPupilosDeApoderado(principal.userId()));
	}

	@GetMapping("/pupilos/{alumnoUsuarioId}/ficha")
	public ResponseEntity<AdministracionDominioService.AlumnoFichaResumen> fichaPupilo(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@PathVariable Long alumnoUsuarioId) {
		if (principal == null || !Roles.isApoderado(principal.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo apoderados");
		}
		return dominio.fichaPupiloApoderado(principal.userId(), alumnoUsuarioId)
				.map(ResponseEntity::ok)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Pupilo no vinculado"));
	}

	@PutMapping("/pupilos/{alumnoUsuarioId}/datos-personales")
	public ResponseEntity<Void> actualizarDatos(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@PathVariable Long alumnoUsuarioId,
			@RequestBody ActualizarDatosPupiloRequest body) {
		if (principal == null || !Roles.isApoderado(principal.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo apoderados");
		}
		try {
			dominio.actualizarDatosPupilo(principal.userId(), alumnoUsuarioId, body.nombre(), body.apellido());
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}
}
