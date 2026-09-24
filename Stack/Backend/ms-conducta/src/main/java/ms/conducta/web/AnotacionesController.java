package ms.conducta.web;

import ms.common.security.MsUserPrincipal;
import ms.conducta.service.ConductaService;
import ms.conducta.service.ConductaService.AnotacionVista;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conducta")
public class AnotacionesController {

	private final ConductaService conductaService;

	public AnotacionesController(ConductaService conductaService) {
		this.conductaService = conductaService;
	}

	public record CrearAnotacionRequest(
			Long alumnoUsuarioId,
			String tipo,
			String texto,
			Long cursoId,
			Long asignaturaId) {
	}

	@PostMapping("/anotaciones")
	public ResponseEntity<AnotacionVista> crear(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
			@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestBody CrearAnotacionRequest body) {
		var guardada = conductaService.registrarAnotacion(authorization, principal, body.alumnoUsuarioId(),
				body.tipo(), body.texto(), body.cursoId(), body.asignaturaId());
		return ResponseEntity.ok(conductaService.toVista(guardada));
	}

	@GetMapping("/alumnos/{alumnoUsuarioId}/anotaciones")
	public ResponseEntity<List<AnotacionVista>> listar(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
			@PathVariable Long alumnoUsuarioId) {
		return ResponseEntity.ok(conductaService.listarPorAlumno(authorization, alumnoUsuarioId));
	}
}
