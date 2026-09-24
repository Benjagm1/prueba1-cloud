package ms.asistencia.client;

import ms.common.resilience.CircuitBreakerOpenException;
import ms.common.resilience.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AdministracionPermisosClient {

	private final RestClient restClient;
	private final CircuitBreakerRegistry circuitBreakerRegistry;

	public AdministracionPermisosClient(
			@Value("${ms.administracion.base-url}") String baseUrl,
			CircuitBreakerRegistry circuitBreakerRegistry) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
		this.circuitBreakerRegistry = circuitBreakerRegistry;
	}

	public void assertPuedeVerAsistenciaDeAlumno(String authorizationHeader, Long alumnoUsuarioId) {
		ejecutarPermiso(authorizationHeader, "Sin permiso para ver asistencia de este alumno",
				"/api/permisos/puedo-ver-asistencia-de/{id}", alumnoUsuarioId);
	}

	public void assertProfesorDictaEnCurso(String authorizationHeader, Long cursoId, Long asignaturaId) {
		ejecutarPermiso(authorizationHeader, "No dicta esta asignatura en este curso",
				"/api/permisos/puedo-registrar-asistencia?cursoId={c}&asignaturaId={a}", cursoId, asignaturaId);
	}

	private void ejecutarPermiso(String authorizationHeader, String mensaje403, String uriTemplate, Object... uriVars) {
		try {
			circuitBreakerRegistry.forService("administracion").executeVoid(() -> restClient.get()
					.uri(uriTemplate, uriVars)
					.header(HttpHeaders.AUTHORIZATION, authorizationHeader)
					.retrieve()
					.toBodilessEntity());
		} catch (CircuitBreakerOpenException e) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
		} catch (RestClientResponseException e) {
			int code = e.getStatusCode().value();
			if (code == 403) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, mensaje403);
			}
			if (code == 401) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
			}
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "MS-Administración no disponible");
		}
	}
}
