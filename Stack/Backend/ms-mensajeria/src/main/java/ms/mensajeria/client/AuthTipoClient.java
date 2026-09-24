package ms.mensajeria.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AuthTipoClient {

	private final RestClient restClient;
	private final String internalToken;

	public AuthTipoClient(@Value("${ms.auth.base-url}") String authBaseUrl,
			@Value("${internal.api.token}") String internalToken) {
		this.restClient = RestClient.builder().baseUrl(authBaseUrl).build();
		this.internalToken = internalToken;
	}

	public String tipoUsuario(Long usuarioId) {
		Map<String, Object> res = restClient.get()
				.uri("/internal/usuarios/{id}/tipo", usuarioId)
				.header("X-Internal-Token", internalToken)
				.retrieve()
				.body(new ParameterizedTypeReference<Map<String, Object>>() {});
		if (res == null || res.get("tipo") == null) {
			throw new IllegalArgumentException("Destinatario no encontrado");
		}
		return res.get("tipo").toString();
	}
}
