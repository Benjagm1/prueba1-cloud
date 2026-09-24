package ms.asistencia.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
public class AdministracionApoderadosClient {

	private final RestClient restClient;
	private final String internalToken;

	public AdministracionApoderadosClient(
			@Value("${ms.administracion.base-url}") String baseUrl,
			@Value("${internal.api.token}") String internalToken) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
		this.internalToken = internalToken;
	}

	public List<Long> listarApoderadosIds(Long alumnoUsuarioId) {
		try {
			List<Long> ids = restClient.get()
					.uri("/internal/alumnos/{id}/apoderados-ids", alumnoUsuarioId)
					.header("X-Internal-Token", internalToken)
					.retrieve()
					.body(new ParameterizedTypeReference<List<Long>>() {});
			return ids != null ? ids : Collections.emptyList();
		} catch (RuntimeException ex) {
			return Collections.emptyList();
		}
	}
}
