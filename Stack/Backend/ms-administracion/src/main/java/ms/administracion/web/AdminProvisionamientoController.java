package ms.administracion.web;

import ms.administracion.client.AuthUsuarioClient;
import ms.administracion.service.AdministracionDominioService;
import ms.administracion.service.AuditoriaService;
import ms.common.security.MsUserPrincipal;
import ms.common.security.Roles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminProvisionamientoController {

	private final AuthUsuarioClient authUsuarioClient;
	private final AdministracionDominioService dominio;
	private final AuditoriaService auditoriaService;

	public AdminProvisionamientoController(
			AuthUsuarioClient authUsuarioClient,
			AdministracionDominioService dominio,
			AuditoriaService auditoriaService) {
		this.authUsuarioClient = authUsuarioClient;
		this.dominio = dominio;
		this.auditoriaService = auditoriaService;
	}

	public record ProvisionarCuentaRequest(
			String nombre,
			String apellido,
			String email,
			String contrasena,
			String rol,
			Long cursoId,
			Long apoderadoUsuarioId,
			Long alumnoUsuarioId,
			Long asignaturaId) {
	}

	@PostMapping("/cuentas/provisionar")
	public ResponseEntity<Map<String, Object>> provisionar(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestBody ProvisionarCuentaRequest body) {
		requerirAdmin(principal);
		validarEntrada(body);
		Map<String, Object> creado = authUsuarioClient.crearUsuario(
				body.nombre(), body.apellido(), body.email(), body.contrasena(), body.rol().trim().toLowerCase());
		if (creado == null || creado.get("id") == null) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo crear el usuario en autenticación");
		}
		Long userId = ((Number) creado.get("id")).longValue();
		String rol = body.rol().trim().toLowerCase();
		try {
			if (Roles.isAlumno(rol)) {
				dominio.inscribirAlumnoEnCurso(userId, body.cursoId(), body.nombre(), body.apellido());
				dominio.vincularApoderadoAlumno(body.apoderadoUsuarioId(), userId);
			} else if (Roles.isProfesor(rol)) {
				dominio.asignarDocenteAsignaturaCurso(userId, body.asignaturaId(), body.cursoId());
			}
		} catch (RuntimeException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Usuario creado (id=" + userId + ") pero falló la configuración: " + e.getMessage());
		}
		auditoriaService.registrarAdmin(principal.userId(), "PROVISIONAR_CUENTA", userId,
				"Cuenta " + rol + " creada: " + body.email().trim());
		AdministracionDominioService.EstadoCuenta estado = dominio.evaluarEstadoCuenta(userId, rol);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
				"usuario", creado,
				"estadoCuenta", Map.of("completa", estado.completa(), "detalle", estado.detalle())));
	}

	private static void validarEntrada(ProvisionarCuentaRequest body) {
		if (body.rol() == null || body.rol().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el rol");
		}
		String rol = body.rol().trim().toLowerCase();
		if (Roles.isAdmin(rol)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol admin no se provisiona por esta vía");
		}
		if (Roles.isAlumno(rol)) {
			if (body.cursoId() == null || body.apoderadoUsuarioId() == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Alumno requiere cursoId y apoderadoUsuarioId");
			}
		} else if (Roles.isApoderado(rol)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Para registrar apoderado use la pestaña Alumno + apoderado (cuentas nuevas vinculadas)");
		} else if (Roles.isProfesor(rol)) {
			if (body.cursoId() == null || body.asignaturaId() == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profesor requiere cursoId y asignaturaId");
			}
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no válido");
		}
	}

	private static void requerirAdmin(MsUserPrincipal principal) {
		if (principal == null || !Roles.isAdmin(principal.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administración escolar");
		}
	}
}
