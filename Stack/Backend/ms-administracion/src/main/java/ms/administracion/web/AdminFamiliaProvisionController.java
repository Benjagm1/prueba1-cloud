package ms.administracion.web;

import ms.administracion.client.AuthUsuarioClient;
import ms.administracion.service.AdministracionDominioService;
import ms.administracion.service.AuditoriaService;
import ms.common.security.MsUserPrincipal;
import ms.common.security.Roles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Resuelve la dependencia circular alumno↔apoderado: ambas cuentas se crean y vinculan en un solo paso.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminFamiliaProvisionController {

	private final AuthUsuarioClient authUsuarioClient;
	private final AdministracionDominioService dominio;
	private final AuditoriaService auditoriaService;

	public AdminFamiliaProvisionController(
			AuthUsuarioClient authUsuarioClient,
			AdministracionDominioService dominio,
			AuditoriaService auditoriaService) {
		this.authUsuarioClient = authUsuarioClient;
		this.dominio = dominio;
		this.auditoriaService = auditoriaService;
	}

	public record PersonaCuenta(String nombre, String apellido, String email, String contrasena) {
	}

	public record ProvisionarFamiliaRequest(PersonaCuenta alumno, PersonaCuenta apoderado, Long cursoId) {
	}

	@PostMapping("/cuentas/provisionar-familia")
	@Transactional
	public ResponseEntity<Map<String, Object>> provisionarFamilia(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestBody ProvisionarFamiliaRequest body) {
		requerirAdmin(principal);
		validar(body);

		Map<String, Object> apoderadoCreado = authUsuarioClient.crearUsuario(
				body.apoderado().nombre(),
				body.apoderado().apellido(),
				body.apoderado().email(),
				body.apoderado().contrasena(),
				Roles.APODERADO);
		Map<String, Object> alumnoCreado = authUsuarioClient.crearUsuario(
				body.alumno().nombre(),
				body.alumno().apellido(),
				body.alumno().email(),
				body.alumno().contrasena(),
				Roles.ALUMNO);

		if (apoderadoCreado == null || apoderadoCreado.get("id") == null
				|| alumnoCreado == null || alumnoCreado.get("id") == null) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudieron crear las cuentas en autenticación");
		}

		Long apoderadoId = ((Number) apoderadoCreado.get("id")).longValue();
		Long alumnoId = ((Number) alumnoCreado.get("id")).longValue();

		dominio.inscribirAlumnoEnCurso(alumnoId, body.cursoId(), body.alumno().nombre(), body.alumno().apellido());
		dominio.vincularApoderadoAlumno(apoderadoId, alumnoId);

		auditoriaService.registrarAdmin(principal.userId(), "PROVISIONAR_FAMILIA", alumnoId,
				"Alumno " + body.alumno().email() + " y apoderado " + body.apoderado().email() + " en curso "
						+ body.cursoId());

		AdministracionDominioService.EstadoCuenta estadoAlumno = dominio.evaluarEstadoCuenta(alumnoId, Roles.ALUMNO);
		AdministracionDominioService.EstadoCuenta estadoApoderado = dominio.evaluarEstadoCuenta(apoderadoId, Roles.APODERADO);

		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
				"alumno", Map.of(
						"usuario", alumnoCreado,
						"estadoCuenta", Map.of("completa", estadoAlumno.completa(), "detalle", estadoAlumno.detalle())),
				"apoderado", Map.of(
						"usuario", apoderadoCreado,
						"estadoCuenta", Map.of("completa", estadoApoderado.completa(), "detalle", estadoApoderado.detalle())),
				"mensaje", "Alumno y apoderado creados y vinculados correctamente. Ambos pueden iniciar sesión."));
	}

	private static void validar(ProvisionarFamiliaRequest body) {
		if (body.cursoId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el curso del alumno");
		}
		validarPersona(body.alumno(), "alumno");
		validarPersona(body.apoderado(), "apoderado");
	}

	private static void validarPersona(PersonaCuenta p, String etiqueta) {
		if (p == null || p.nombre() == null || p.nombre().isBlank()
				|| p.apellido() == null || p.apellido().isBlank()
				|| p.email() == null || p.email().isBlank()
				|| p.contrasena() == null || p.contrasena().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Datos incompletos para la cuenta de " + etiqueta);
		}
	}

	private static void requerirAdmin(MsUserPrincipal principal) {
		if (principal == null || !Roles.isAdmin(principal.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administración escolar");
		}
	}
}
