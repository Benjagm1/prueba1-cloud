package ms.conducta.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthUsuarioInternoClient {

	private final RestClient restClient;
	private final String internalToken;
	private final Map<Long, String> cacheNombres = new ConcurrentHashMap<>();

	public AuthUsuarioInternoClient(
			@Value("${ms.autenticacion.base-url}") String baseUrl,
			@Value("${internal.api.token}") String internalToken) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
		this.internalToken = internalToken;
	}

	public String nombreCompletoProfesor(Long usuarioId) {
		if (usuarioId == null) {
			return "—";
		}
		return cacheNombres.computeIfAbsent(usuarioId, this::fetchNombreCompleto);
	}

	private String fetchNombreCompleto(Long usuarioId) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> body = restClient.get()
					.uri("/internal/usuarios/{id}/contacto", usuarioId)
					.header("X-Internal-Token", internalToken)
					.retrieve()
					.body(Map.class);
			if (body != null) {
				String nombre = String.valueOf(body.getOrDefault("nombre", "")).trim();
				String apellido = String.valueOf(body.getOrDefault("apellido", "")).trim();
				String completo = (nombre + " " + apellido).trim();
				if (!completo.isBlank()) {
					return completo;
				}
			}
		} catch (Exception ignored) {
			// fallback
		}
		return "Docente #" + usuarioId;
	}
}
