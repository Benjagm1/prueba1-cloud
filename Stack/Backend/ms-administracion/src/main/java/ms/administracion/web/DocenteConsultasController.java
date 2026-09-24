package ms.administracion.web;

import ms.administracion.client.AuthContactoClient;
import ms.administracion.model.AlumnoCurso;
import ms.administracion.model.ApoderadoAlumno;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/docentes")
public class DocenteConsultasController {

	private final AdministracionDominioService dominio;
	private final AuthContactoClient authContactoClient;

	public DocenteConsultasController(AdministracionDominioService dominio, AuthContactoClient authContactoClient) {
		this.dominio = dominio;
		this.authContactoClient = authContactoClient;
	}

	@GetMapping("/contactos-mensajeria")
	public ResponseEntity<List<AdministracionDominioService.ContactoMensajeria>> contactosMensajeria(
			@AuthenticationPrincipal MsUserPrincipal principal) {
		if (principal == null || !Roles.isProfesor(principal.tipo())) {
			return ResponseEntity.status(403).build();
		}
		return ResponseEntity.ok(dominio.contactosMensajeriaProfesor(principal.userId()));
	}

	@GetMapping("/mis-asignaciones")
	public ResponseEntity<List<AdministracionDominioService.AsignacionDocenteResumen>> misAsignaciones(
			@AuthenticationPrincipal MsUserPrincipal principal) {
		if (principal == null || !Roles.isProfesor(principal.tipo())) {
			return ResponseEntity.status(403).build();
		}
		return ResponseEntity.ok(dominio.listarAsignacionesDeProfesor(principal.userId()));
	}

	@GetMapping("/lista-alumnos")
	public ResponseEntity<List<AlumnoListaResponse>> listaAlumnos(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestParam Long cursoId,
			@RequestParam Long asignaturaId) {
		if (principal == null || !Roles.isProfesor(principal.tipo())) {
			return ResponseEntity.status(403).build();
		}
		if (!dominio.profesorDictaAsignaturaEnCurso(principal.userId(), asignaturaId, cursoId)) {
			return ResponseEntity.status(403).build();
		}
		List<AlumnoCurso> rows = dominio.listaAlumnosCursoOrdenada(cursoId);
		List<AlumnoListaResponse> out = rows.stream()
				.map(r -> new AlumnoListaResponse(r.getAlumnoUsuarioId(), r.getNombre(), r.getApellido()))
				.toList();
		return ResponseEntity.ok(out);
	}

	public record AlumnoListaResponse(Long alumnoUsuarioId, String nombre, String apellido) {
	}

	@GetMapping("/alumnos/{alumnoUsuarioId}/contacto-apoderados")
	public ResponseEntity<List<Map<String, Object>>> contactoApoderados(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@PathVariable Long alumnoUsuarioId) {
		if (principal == null || !Roles.isProfesor(principal.tipo())) {
			return ResponseEntity.status(403).build();
		}
		if (!dominio.profesorTieneAlumnoEnSuCurso(principal.userId(), alumnoUsuarioId)) {
			return ResponseEntity.status(403).build();
		}
		dominio.registrarAuditoriaContactoApoderado(principal.userId(), alumnoUsuarioId);
		List<Map<String, Object>> salida = new ArrayList<>();
		for (ApoderadoAlumno v : dominio.apoderadosDelAlumno(alumnoUsuarioId)) {
			Map<String, Object> c = authContactoClient.obtenerContactoUsuario(v.getApoderadoUsuarioId());
			if (c != null) {
				salida.add(c);
			}
		}
		return ResponseEntity.ok(salida);
	}
}
