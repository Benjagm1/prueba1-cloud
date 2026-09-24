package ms.mensajeria.web;

import ms.common.security.MsUserPrincipal;
import ms.mensajeria.model.Notificacion;
import ms.mensajeria.service.NotificacionesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
public class BandejaController {

	private final NotificacionesService notificacionesService;

	public BandejaController(NotificacionesService notificacionesService) {
		this.notificacionesService = notificacionesService;
	}

	@GetMapping("/mis-notificaciones")
	public ResponseEntity<List<Notificacion>> misNotificaciones(@AuthenticationPrincipal MsUserPrincipal principal) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.ok(notificacionesService.bandeja(principal.userId()));
	}

	@PostMapping("/{id}/leer")
	public ResponseEntity<Void> marcarLeida(@AuthenticationPrincipal MsUserPrincipal principal, @PathVariable Long id) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		notificacionesService.marcarLeida(principal.userId(), id);
		return ResponseEntity.noContent().build();
	}
}
