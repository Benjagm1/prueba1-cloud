package ms.auth.client;

import ms.common.resilience.CircuitBreakerOpenException;
import ms.common.resilience.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class AdministracionEstadoCuentaClient {

	private final RestClient restClient;
	private final String internalToken;
	private final CircuitBreakerRegistry circuitBreakerRegistry;

	public AdministracionEstadoCuentaClient(
			@Value("${ms.administracion.base-url}") String adminBaseUrl,
			@Value("${internal.api.token}") String internalToken,
			CircuitBreakerRegistry circuitBreakerRegistry) {
		this.restClient = RestClient.builder().baseUrl(adminBaseUrl).build();
		this.internalToken = internalToken;
		this.circuitBreakerRegistry = circuitBreakerRegistry;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> estadoCuenta(Long usuarioId, String tipo) {
		try {
			return circuitBreakerRegistry.forService("administracion").execute(() -> restClient.get()
					.uri("/internal/usuarios/{id}/estado-cuenta?tipo={tipo}", usuarioId, tipo)
					.header("X-Internal-Token", internalToken)
					.retrieve()
					.body(Map.class));
		} catch (CircuitBreakerOpenException e) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
		}
	}
}
