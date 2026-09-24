package ms.conducta.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class AdministracionConductaClient {

	private final RestClient restClient;

	public AdministracionConductaClient(@Value("${ms.administracion.base-url}") String baseUrl) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
	}

	public void assertProfesorPuedeGestionarAlumno(String authorizationHeader, Long alumnoUsuarioId) {
		try {
			restClient.get()
					.uri("/api/permisos/profesor-puede-gestionar-alumno/{id}", alumnoUsuarioId)
					.header(HttpHeaders.AUTHORIZATION, authorizationHeader)
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException e) {
			int code = e.getStatusCode().value();
			if (code == 403 || code == 401) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puede registrar anotación para este alumno");
			}
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "MS-Administración no disponible");
		}
	}

	public List<Long> listarApoderadosIds(Long alumnoUsuarioId, String internalToken) {
		return restClient.get()
				.uri("/internal/alumnos/{id}/apoderados-ids", alumnoUsuarioId)
				.header("X-Internal-Token", internalToken)
				.retrieve()
				.body(new ParameterizedTypeReference<List<Long>>() {});
	}
}
