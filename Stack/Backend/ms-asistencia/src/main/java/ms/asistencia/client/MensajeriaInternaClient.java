package ms.asistencia.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MensajeriaInternaClient {

	private final RestClient restClient;
	private final String internalToken;

	public MensajeriaInternaClient(@Value("${ms.mensajeria.base-url}") String baseUrl,
			@Value("${internal.api.token}") String internalToken) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
		this.internalToken = internalToken;
	}

	public record CrearNotificacionPayload(Long destinatarioUsuarioId, String titulo, String cuerpo) {
	}

	public void crearNotificacion(Long destinatarioUsuarioId, String titulo, String cuerpo) {
		try {
			restClient.post()
					.uri("/internal/notificaciones")
					.header("X-Internal-Token", internalToken)
					.contentType(MediaType.APPLICATION_JSON)
					.body(new CrearNotificacionPayload(destinatarioUsuarioId, titulo, cuerpo))
					.retrieve()
					.toBodilessEntity();
		} catch (RuntimeException ignored) {
			// best-effort
		}
	}
}
