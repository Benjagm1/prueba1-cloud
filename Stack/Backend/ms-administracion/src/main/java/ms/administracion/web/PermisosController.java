package ms.administracion.web;

import ms.administracion.service.AdministracionDominioService;
import ms.common.security.MsUserPrincipal;
import ms.common.security.Roles;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permisos")
public class PermisosController {

	private final AdministracionDominioService dominio;

	public PermisosController(AdministracionDominioService dominio) {
		this.dominio = dominio;
	}

	@GetMapping("/puedo-ver-notas-de/{alumnoUsuarioId}")
	public ResponseEntity<Void> puedoVerNotas(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@PathVariable Long alumnoUsuarioId) {
		return evaluarAccesoDatosAlumno(principal, alumnoUsuarioId);
	}

	@GetMapping("/puedo-ver-asistencia-de/{alumnoUsuarioId}")
	public ResponseEntity<Void> puedoVerAsistencia(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@PathVariable Long alumnoUsuarioId) {
		return evaluarAccesoDatosAlumno(principal, alumnoUsuarioId);
	}

	@GetMapping("/puedo-registrar-asistencia")
	public ResponseEntity<Void> puedoRegistrarAsistencia(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestParam Long cursoId,
			@RequestParam Long asignaturaId) {
		if (principal == null || !Roles.isProfesor(principal.tipo())) {
			return ResponseEntity.status(403).build();
		}
		if (dominio.profesorDictaAsignaturaEnCurso(principal.userId(), asignaturaId, cursoId)) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.status(403).build();
	}

	@GetMapping("/profesor-puede-gestionar-alumno/{alumnoUsuarioId}")
	public ResponseEntity<Void> profesorPuedeGestionarAlumno(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@PathVariable Long alumnoUsuarioId) {
		if (principal == null || !Roles.isProfesor(principal.tipo())) {
			return ResponseEntity.status(403).build();
		}
		if (dominio.profesorTieneAlumnoEnSuCurso(principal.userId(), alumnoUsuarioId)) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.status(403).build();
	}

	private ResponseEntity<Void> evaluarAccesoDatosAlumno(MsUserPrincipal principal, Long alumnoUsuarioId) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		if (dominio.puedeVerNotasDeAlumno(principal, alumnoUsuarioId)) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.status(403).build();
	}
}
