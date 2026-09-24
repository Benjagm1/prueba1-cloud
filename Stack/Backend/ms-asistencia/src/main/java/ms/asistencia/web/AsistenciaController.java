package ms.asistencia.web;

import ms.asistencia.realtime.AsistenciaSseHub;
import ms.asistencia.service.AsistenciaService;
import ms.asistencia.service.AsistenciaService.HistorialAsistenciaItem;
import ms.common.security.MsUserPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/asistencia")
public class AsistenciaController {

	private final AsistenciaService asistenciaService;
	private final AsistenciaSseHub sseHub;

	public AsistenciaController(AsistenciaService asistenciaService, AsistenciaSseHub sseHub) {
		this.asistenciaService = asistenciaService;
		this.sseHub = sseHub;
	}

	public record GuardarDiaRequest(LocalDate fecha, Long cursoId, Long asignaturaId, List<AsistenciaService.FilaAsistencia> filas) {
	}

	@GetMapping("/dia")
	public ResponseEntity<List<AsistenciaService.RegistroDiaDto>> consultarDia(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
			@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
			@RequestParam Long cursoId,
			@RequestParam Long asignaturaId) {
		return ResponseEntity.ok(asistenciaService.consultarDia(authorization, principal, fecha, cursoId, asignaturaId));
	}

	@PostMapping("/dia")
	public ResponseEntity<Void> guardarDia(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
			@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestBody GuardarDiaRequest body) {
		asistenciaService.guardarDia(authorization, principal, body.fecha(), body.cursoId(), body.asignaturaId(),
				body.filas());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/alumnos/{alumnoUsuarioId}/historial")
	public ResponseEntity<List<HistorialAsistenciaItem>> historial(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
			@AuthenticationPrincipal MsUserPrincipal principal,
			@PathVariable Long alumnoUsuarioId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
		return ResponseEntity.ok(asistenciaService.historial(authorization, principal, alumnoUsuarioId, desde, hasta));
	}

	@GetMapping("/mi-resumen")
	public ResponseEntity<AsistenciaService.ResumenAsistenciaDto> miResumen(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
		return ResponseEntity.ok(asistenciaService.resumenPropio(principal, desde, hasta));
	}

	@GetMapping("/docente/resumen")
	public ResponseEntity<AsistenciaService.ResumenAsistenciaDto> resumenDocente(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
		return ResponseEntity.ok(asistenciaService.resumenDocente(principal, desde, hasta));
	}

	@GetMapping(value = "/stream/{alumnoUsuarioId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter stream(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
			@AuthenticationPrincipal MsUserPrincipal principal,
			@PathVariable Long alumnoUsuarioId) {
		asistenciaService.asegurarAccesoAsistenciaAlumno(authorization, principal, alumnoUsuarioId);
		return sseHub.suscribir(alumnoUsuarioId);
	}
}
