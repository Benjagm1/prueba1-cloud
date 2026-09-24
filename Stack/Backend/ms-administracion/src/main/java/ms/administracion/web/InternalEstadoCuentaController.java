package ms.administracion.web;

import ms.administracion.service.AdministracionDominioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/usuarios")
public class InternalEstadoCuentaController {

	private final AdministracionDominioService dominio;
	private final String internalToken;

	public InternalEstadoCuentaController(AdministracionDominioService dominio,
			@Value("${internal.api.token}") String internalToken) {
		this.dominio = dominio;
		this.internalToken = internalToken;
	}

	@GetMapping("/{id}/estado-cuenta")
	public ResponseEntity<Map<String, Object>> estadoCuenta(
			@PathVariable Long id,
			@RequestParam String tipo,
			@RequestHeader(value = "X-Internal-Token", required = false) String token) {
		if (token == null || !token.equals(internalToken)) {
			return ResponseEntity.status(403).build();
		}
		AdministracionDominioService.EstadoCuenta estado = dominio.evaluarEstadoCuenta(id, tipo);
		return ResponseEntity.ok(Map.of("completa", estado.completa(), "detalle", estado.detalle()));
	}
}
