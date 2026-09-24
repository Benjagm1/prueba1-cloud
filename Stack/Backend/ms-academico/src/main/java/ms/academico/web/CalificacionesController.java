package ms.academico.web;

import ms.academico.model.Calificacion;
import ms.academico.service.CalificacionesService;
import ms.common.security.MsUserPrincipal;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/academico")
public class CalificacionesController {

	private final CalificacionesService calificacionesService;

	public CalificacionesController(CalificacionesService calificacionesService) {
		this.calificacionesService = calificacionesService;
	}

	public record RegistrarCalificacionRequest(Long alumnoUsuarioId, Long cursoId, Long asignaturaId,
			String nombreEvaluacion, BigDecimal nota) {
	}

	@GetMapping("/mis-notas")
	public ResponseEntity<List<Calificacion>> misNotas(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
			@AuthenticationPrincipal MsUserPrincipal principal) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.ok(calificacionesService.listarNotasAlumno(authorization, principal, principal.userId()));
	}

	@GetMapping("/alumnos/{alumnoUsuarioId}/notas")
	public ResponseEntity<List<Calificacion>> notasDeAlumno(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
			@AuthenticationPrincipal MsUserPrincipal principal,
			@PathVariable Long alumnoUsuarioId) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.ok(calificacionesService.listarNotasAlumno(authorization, principal, alumnoUsuarioId));
	}

	@PostMapping("/calificaciones")
	public ResponseEntity<Calificacion> registrar(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
			@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestBody RegistrarCalificacionRequest body) {
		Calificacion guardada = calificacionesService.registrar(authorization, principal, body.alumnoUsuarioId(),
				body.cursoId(), body.asignaturaId(), body.nombreEvaluacion(), body.nota());
		return ResponseEntity.ok(guardada);
	}
}
