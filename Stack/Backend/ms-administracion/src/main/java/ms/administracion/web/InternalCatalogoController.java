package ms.administracion.web;

import java.util.Map;

import ms.administracion.model.Asignatura;
import ms.administracion.repo.AsignaturaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class InternalCatalogoController {

	private final AsignaturaRepository asignaturaRepository;
	private final String internalToken;

	public InternalCatalogoController(
			AsignaturaRepository asignaturaRepository,
			@Value("${internal.api.token}") String internalToken) {
		this.asignaturaRepository = asignaturaRepository;
		this.internalToken = internalToken;
	}

	@GetMapping("/asignaturas/{id}")
	public ResponseEntity<Map<String, Object>> asignaturaPorId(
			@PathVariable Long id,
			@RequestHeader(value = "X-Internal-Token", required = false) String token) {
		if (!tokenValido(token)) {
			return ResponseEntity.status(403).build();
		}
		return asignaturaRepository.findById(id)
				.map(InternalCatalogoController::mapAsignatura)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	private static Map<String, Object> mapAsignatura(Asignatura a) {
		return Map.of(
				"id", a.getId(),
				"codigo", a.getCodigo(),
				"nombre", a.getNombre());
	}

	private boolean tokenValido(String token) {
		return token != null && token.equals(internalToken);
	}
}
