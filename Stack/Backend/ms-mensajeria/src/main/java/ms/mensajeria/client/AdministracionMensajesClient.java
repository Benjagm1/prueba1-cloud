package ms.mensajeria.client;

import ms.common.resilience.CircuitBreakerOpenException;
import ms.common.resilience.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class AdministracionMensajesClient {

	private final RestClient restClient;
	private final String internalToken;
	private final CircuitBreakerRegistry circuitBreakerRegistry;

	public AdministracionMensajesClient(@Value("${ms.administracion.base-url}") String adminBaseUrl,
			@Value("${internal.api.token}") String internalToken,
			CircuitBreakerRegistry circuitBreakerRegistry) {
		this.restClient = RestClient.builder().baseUrl(adminBaseUrl).build();
		this.internalToken = internalToken;
		this.circuitBreakerRegistry = circuitBreakerRegistry;
	}

	public boolean puedeEnviar(Long de, String deTipo, Long para, String paraTipo) {
		Map<String, Boolean> res;
		try {
			res = circuitBreakerRegistry.forService("administracion").execute(() -> restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/internal/mensajes/puede-enviar")
						.queryParam("de", de)
						.queryParam("deTipo", deTipo)
						.queryParam("para", para)
						.queryParam("paraTipo", paraTipo)
						.build())
				.header("X-Internal-Token", internalToken)
				.retrieve()
				.body(new ParameterizedTypeReference<Map<String, Boolean>>() {}));
		} catch (CircuitBreakerOpenException e) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
		}
		return res != null && Boolean.TRUE.equals(res.get("permitido"));
	}
}
