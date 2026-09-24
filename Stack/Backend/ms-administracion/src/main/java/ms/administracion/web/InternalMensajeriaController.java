package ms.administracion.web;

import ms.administracion.service.AdministracionDominioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/mensajes")
public class InternalMensajeriaController {

	private final AdministracionDominioService dominio;
	private final String internalToken;

	public InternalMensajeriaController(AdministracionDominioService dominio,
			@Value("${internal.api.token}") String internalToken) {
		this.dominio = dominio;
		this.internalToken = internalToken;
	}

	@GetMapping("/puede-enviar")
	public ResponseEntity<Map<String, Boolean>> puedeEnviar(
			@RequestParam Long de,
			@RequestParam String deTipo,
			@RequestParam Long para,
			@RequestParam String paraTipo,
			@RequestHeader(value = "X-Internal-Token", required = false) String token) {
		if (token == null || !token.equals(internalToken)) {
			return ResponseEntity.status(403).build();
		}
		boolean ok = dominio.puedeEnviarMensaje(de, deTipo, para, paraTipo);
		return ResponseEntity.ok(Map.of("permitido", ok));
	}
}
