package ms.conducta.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AdministracionAccesoAlumnoClient {

	private final RestClient restClient;

	public AdministracionAccesoAlumnoClient(@Value("${ms.administracion.base-url}") String baseUrl) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
	}

	public void assertPuedeVerDatosAlumno(String authorizationHeader, Long alumnoUsuarioId) {
		try {
			restClient.get()
					.uri("/api/permisos/puedo-ver-notas-de/{id}", alumnoUsuarioId)
					.header(HttpHeaders.AUTHORIZATION, authorizationHeader)
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException e) {
			int code = e.getStatusCode().value();
			if (code == 403 || code == 401) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin permiso para ver conducta del alumno");
			}
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "MS-Administración no disponible");
		}
	}
}
