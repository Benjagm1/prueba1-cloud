package ms.mensajeria.web;

import ms.mensajeria.model.Notificacion;
import ms.mensajeria.service.NotificacionesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class InternalNotificacionesController {

	private final NotificacionesService notificacionesService;
	private final String internalToken;

	public InternalNotificacionesController(NotificacionesService notificacionesService,
			@Value("${internal.api.token}") String internalToken) {
		this.notificacionesService = notificacionesService;
		this.internalToken = internalToken;
	}

	public record CrearNotificacionRequest(Long destinatarioUsuarioId, String titulo, String cuerpo) {
	}

	@PostMapping("/notificaciones")
	public ResponseEntity<Notificacion> crear(
			@RequestHeader(value = "X-Internal-Token", required = false) String token,
			@RequestBody CrearNotificacionRequest body) {
		if (token == null || !token.equals(internalToken)) {
			return ResponseEntity.status(403).build();
		}
		return ResponseEntity.ok(notificacionesService.crearInterno(body.destinatarioUsuarioId(), body.titulo(),
				body.cuerpo()));
	}
}
