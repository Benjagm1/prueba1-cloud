package ms.administracion.web;

import ms.administracion.model.Asignatura;
import ms.administracion.model.Curso;
import ms.administracion.service.AdministracionDominioService;
import ms.administracion.service.AuditoriaService;
import ms.common.security.MsUserPrincipal;
import ms.common.security.Roles;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminComandosController {

	private final AdministracionDominioService dominio;
	private final AuditoriaService auditoriaService;

	public AdminComandosController(AdministracionDominioService dominio, AuditoriaService auditoriaService) {
		this.dominio = dominio;
		this.auditoriaService = auditoriaService;
	}

	public record InscripcionAlumnoRequest(Long alumnoUsuarioId, Long cursoId, String nombre, String apellido) {
	}

	public record VinculoApoderadoRequest(Long apoderadoUsuarioId, Long alumnoUsuarioId) {
	}

	public record AsignacionDocenteRequest(Long profesorUsuarioId, Long asignaturaId, Long cursoId) {
	}

	@GetMapping("/catalogo/cursos")
	public List<Curso> cursos(@AuthenticationPrincipal MsUserPrincipal principal) {
		requerirAdmin(principal);
		return dominio.listarCursos();
	}

	@GetMapping("/catalogo/asignaturas")
	public List<Asignatura> asignaturas(@AuthenticationPrincipal MsUserPrincipal principal) {
		requerirAdmin(principal);
		return dominio.listarAsignaturas();
	}

	@PostMapping("/alumnos/inscripcion")
	public ResponseEntity<Void> inscribir(@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestBody InscripcionAlumnoRequest body) {
		requerirAdmin(principal);
		try {
			dominio.inscribirAlumnoEnCurso(body.alumnoUsuarioId(), body.cursoId(), body.nombre(), body.apellido());
		} catch (IllegalStateException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/apoderados/vinculos")
	public ResponseEntity<Void> vincularApoderado(@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestBody VinculoApoderadoRequest body) {
		requerirAdmin(principal);
		dominio.vincularApoderadoAlumno(body.apoderadoUsuarioId(), body.alumnoUsuarioId());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/docentes/{profesorUsuarioId}/asignaciones")
	public List<AdministracionDominioService.AsignacionDocenteResumen> asignacionesProfesor(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@PathVariable Long profesorUsuarioId) {
		requerirAdmin(principal);
		return dominio.listarAsignacionesDeProfesor(profesorUsuarioId);
	}

	@PostMapping("/docentes/asignaciones")
	public ResponseEntity<Void> asignarDocente(@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestBody AsignacionDocenteRequest body) {
		requerirAdmin(principal);
		try {
			dominio.asignarDocenteAsignaturaCurso(body.profesorUsuarioId(), body.asignaturaId(), body.cursoId());
			auditoriaService.registrarAdmin(principal.userId(), "ASIGNAR_DOCENTE", body.profesorUsuarioId(),
					"Curso " + body.cursoId() + ", asignatura " + body.asignaturaId());
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}

	private static void requerirAdmin(MsUserPrincipal principal) {
		if (principal == null || !Roles.isAdmin(principal.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administración escolar");
		}
	}
}
