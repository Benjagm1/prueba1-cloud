package ms.administracion.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AuthContactoClient {

	private final RestClient restClient;
	private final String internalToken;

	public AuthContactoClient(@Value("${ms.auth.base-url}") String authBaseUrl,
			@Value("${internal.api.token}") String internalToken) {
		this.restClient = RestClient.builder().baseUrl(authBaseUrl).build();
		this.internalToken = internalToken;
	}

	public Map<String, Object> obtenerContactoUsuario(Long usuarioId) {
		return restClient.get()
				.uri("/internal/usuarios/{id}/contacto", usuarioId)
				.header("X-Internal-Token", internalToken)
				.retrieve()
				.body(new ParameterizedTypeReference<Map<String, Object>>() {});
	}
}
