package ms.administracion.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AuthUsuarioClient {

	private final RestClient restClient;
	private final String internalToken;

	public AuthUsuarioClient(@Value("${ms.auth.base-url}") String authBaseUrl,
			@Value("${internal.api.token}") String internalToken) {
		this.restClient = RestClient.builder().baseUrl(authBaseUrl).build();
		this.internalToken = internalToken;
	}

	public Map<String, Object> crearUsuario(String nombre, String apellido, String email, String contrasena, String tipo) {
		Map<String, Object> body = Map.of(
				"nombre", nombre,
				"apellido", apellido,
				"email", email,
				"contrasena", contrasena,
				"tipo", tipo);
		return restClient.post()
				.uri("/internal/usuarios")
				.header("X-Internal-Token", internalToken)
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve()
				.body(new ParameterizedTypeReference<Map<String, Object>>() {});
	}

	public String tipoUsuario(Long usuarioId) {
		Map<String, Object> res = restClient.get()
				.uri("/internal/usuarios/{id}/tipo", usuarioId)
				.header("X-Internal-Token", internalToken)
				.retrieve()
				.body(new ParameterizedTypeReference<Map<String, Object>>() {});
		if (res == null || res.get("tipo") == null) {
			throw new IllegalArgumentException("Usuario no encontrado en autenticación");
		}
		return res.get("tipo").toString();
	}

	public void sincronizarDemo(Long usuarioId, String nombre, String apellido, String contrasena) {
		sincronizarDemo(usuarioId, nombre, apellido, contrasena, null);
	}

	public void sincronizarDemo(Long usuarioId, String nombre, String apellido, String contrasena, String email) {
		try {
			java.util.HashMap<String, String> body = new java.util.HashMap<>();
			body.put("nombre", nombre);
			body.put("apellido", apellido);
			body.put("contrasena", contrasena != null ? contrasena : "");
			if (email != null && !email.isBlank()) {
				body.put("email", email.trim().toLowerCase());
			}
			restClient.put()
					.uri("/internal/usuarios/{id}/sincronizar-demo", usuarioId)
					.header("X-Internal-Token", internalToken)
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.toBodilessEntity();
		} catch (RuntimeException ex) {
			// demo seed best-effort
		}
	}

	public void sincronizarNombre(Long usuarioId, String nombre, String apellido) {
		sincronizarDemo(usuarioId, nombre, apellido, "");
	}

	public Long buscarIdPorEmail(String email) {
		try {
			Map<String, Object> res = restClient.get()
					.uri("/internal/usuarios/by-email/{email}", email.trim().toLowerCase())
					.header("X-Internal-Token", internalToken)
					.retrieve()
					.body(new ParameterizedTypeReference<Map<String, Object>>() {});
			if (res == null || res.get("id") == null) {
				return null;
			}
			return ((Number) res.get("id")).longValue();
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		}
	}
}
