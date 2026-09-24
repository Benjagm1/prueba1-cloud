package ms.asistencia.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AdministracionCatalogoInternoClient {

	private final RestClient restClient;
	private final String internalToken;
	private final Map<Long, String> cacheNombres = new ConcurrentHashMap<>();

	public AdministracionCatalogoInternoClient(
			@Value("${ms.administracion.base-url}") String baseUrl,
			@Value("${internal.api.token}") String internalToken) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
		this.internalToken = internalToken;
	}

	public String nombreAsignatura(Long asignaturaId) {
		if (asignaturaId == null) {
			return "—";
		}
		return cacheNombres.computeIfAbsent(asignaturaId, this::fetchNombreAsignatura);
	}

	private String fetchNombreAsignatura(Long asignaturaId) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> body = restClient.get()
					.uri("/internal/asignaturas/{id}", asignaturaId)
					.header("X-Internal-Token", internalToken)
					.retrieve()
					.body(Map.class);
			if (body != null && body.get("nombre") != null) {
				return String.valueOf(body.get("nombre"));
			}
		} catch (Exception ignored) {
			// fallback
		}
		return "Asignatura #" + asignaturaId;
	}
}
