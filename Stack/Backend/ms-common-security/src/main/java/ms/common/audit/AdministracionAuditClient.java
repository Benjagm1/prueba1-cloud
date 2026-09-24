package ms.common.audit;

import java.util.Optional;

import ms.common.resilience.CircuitBreakerRegistry;
import ms.common.resilience.CircuitBreakerOpenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Publica eventos de auditoría al microservicio de Administración (fire-and-forget).
 */
@Component
@ConditionalOnProperty(name = "ms.administracion.base-url")
public class AdministracionAuditClient {

	private static final Logger log = LoggerFactory.getLogger(AdministracionAuditClient.class);

	private final RestClient restClient;
	private final String internalToken;
	private final CircuitBreakerRegistry circuitBreakerRegistry;

	public AdministracionAuditClient(
			@Value("${ms.administracion.base-url}") String baseUrl,
			@Value("${internal.api.token:dev-internal-token}") String internalToken,
			CircuitBreakerRegistry circuitBreakerRegistry) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
		this.internalToken = internalToken;
		this.circuitBreakerRegistry = circuitBreakerRegistry;
	}

	public void registrar(AuditEventRequest evento) {
		try {
			circuitBreakerRegistry.forService("administracion-audit").executeVoid(() -> restClient.post()
					.uri("/internal/auditoria")
					.header("X-Internal-Token", internalToken)
					.contentType(MediaType.APPLICATION_JSON)
					.body(evento)
					.retrieve()
					.toBodilessEntity());
		} catch (CircuitBreakerOpenException ex) {
			log.warn("Auditoría diferida (circuit breaker): {} — {}", evento.accion(), evento.detalle());
		} catch (Exception ex) {
			log.warn("No se pudo registrar auditoría: {} — {}", evento.accion(), ex.getMessage());
		}
	}

	public void registrarSilencioso(String modulo, String accion, Long actorId, String actorTipo, String detalle) {
		registrar(new AuditEventRequest(modulo, accion, actorId, actorTipo, null, detalle));
	}

	public void registrarSilencioso(
			String modulo, String accion, Long actorId, String actorTipo, Long recursoId, String detalle) {
		registrar(new AuditEventRequest(modulo, accion, actorId, actorTipo, recursoId, detalle));
	}

	public Optional<String> internalToken() {
		return Optional.ofNullable(internalToken);
	}
}
