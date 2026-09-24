package ms.administracion.web;

import ms.administracion.repo.ApoderadoAlumnoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal")
public class InternalApoderadosController {

	private final ApoderadoAlumnoRepository apoderadoAlumnoRepository;
	private final String internalToken;

	public InternalApoderadosController(ApoderadoAlumnoRepository apoderadoAlumnoRepository,
			@Value("${internal.api.token}") String internalToken) {
		this.apoderadoAlumnoRepository = apoderadoAlumnoRepository;
		this.internalToken = internalToken;
	}

	@GetMapping("/alumnos/{alumnoUsuarioId}/apoderados-ids")
	public ResponseEntity<List<Long>> apoderadosIds(
			@RequestHeader(value = "X-Internal-Token", required = false) String token,
			@PathVariable Long alumnoUsuarioId) {
		if (token == null || !token.equals(internalToken)) {
			return ResponseEntity.status(403).build();
		}
		List<Long> ids = apoderadoAlumnoRepository.findByAlumnoUsuarioId(alumnoUsuarioId).stream()
				.map(a -> a.getApoderadoUsuarioId())
				.toList();
		return ResponseEntity.ok(ids);
	}
}
