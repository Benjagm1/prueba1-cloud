package ms.mensajeria.web;

import ms.common.security.MsUserPrincipal;
import ms.mensajeria.model.Mensaje;
import ms.mensajeria.service.MensajesDirectosService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
public class MensajesDirectosController {

	private final MensajesDirectosService mensajesDirectosService;

	public MensajesDirectosController(MensajesDirectosService mensajesDirectosService) {
		this.mensajesDirectosService = mensajesDirectosService;
	}

	public record EnviarMensajeRequest(Long destinatarioUsuarioId, String asunto, String cuerpo) {
	}

	@PostMapping("/enviar")
	public ResponseEntity<Mensaje> enviar(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestBody EnviarMensajeRequest body) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		if (body.destinatarioUsuarioId() == null || body.cuerpo() == null || body.cuerpo().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "destinatarioUsuarioId y cuerpo son obligatorios");
		}
		try {
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(mensajesDirectosService.enviar(principal, body.destinatarioUsuarioId(), body.asunto(), body.cuerpo()));
		} catch (IllegalStateException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/recibidos")
	public ResponseEntity<List<MensajesDirectosService.MensajeBandejaDto>> recibidos(
			@AuthenticationPrincipal MsUserPrincipal principal) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.ok(mensajesDirectosService.recibidos(principal.userId()));
	}

	@GetMapping("/enviados")
	public ResponseEntity<List<Mensaje>> enviados(@AuthenticationPrincipal MsUserPrincipal principal) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.ok(mensajesDirectosService.enviados(principal.userId()));
	}

	@PostMapping("/mensajes-directos/{id}/leer")
	public ResponseEntity<Void> marcarLeido(@AuthenticationPrincipal MsUserPrincipal principal, @PathVariable Long id) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		try {
			mensajesDirectosService.marcarMensajeLeido(principal.userId(), id);
		} catch (IllegalStateException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
		}
		return ResponseEntity.noContent().build();
	}
}
