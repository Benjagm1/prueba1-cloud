package ms.administracion.web;

import ms.administracion.service.AuditoriaService;
import ms.common.audit.AuditEventRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/auditoria")
public class InternalAuditoriaController {

	private final AuditoriaService auditoriaService;
	private final String internalToken;

	public InternalAuditoriaController(
			AuditoriaService auditoriaService,
			@Value("${internal.api.token}") String internalToken) {
		this.auditoriaService = auditoriaService;
		this.internalToken = internalToken;
	}

	@PostMapping
	public ResponseEntity<Void> registrar(
			@RequestHeader(value = "X-Internal-Token", required = false) String token,
			@RequestBody AuditEventRequest evento) {
		validarToken(token);
		auditoriaService.registrar(evento);
		return ResponseEntity.noContent().build();
	}

	private void validarToken(String token) {
		if (token == null || !token.equals(internalToken)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token interno inválido");
		}
	}
}
