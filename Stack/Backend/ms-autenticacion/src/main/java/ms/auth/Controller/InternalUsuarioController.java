package ms.auth.Controller;

import ms.auth.Model.Usuario;
import ms.auth.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import ms.auth.Service.AuthService;
import ms.dto.CrearUsuarioRequest;
import ms.dto.CrearUsuarioResponse;
import ms.dto.SincronizarDemoRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Llamadas servidor-a-servidor (p. ej. MS-Administración para HU5). Protegidas por cabecera compartida.
 */
@RestController
@RequestMapping("/internal")
public class InternalUsuarioController {

	private final UsuarioRepository usuarioRepository;
	private final AuthService authService;
	private final String internalToken;

	public InternalUsuarioController(UsuarioRepository usuarioRepository, AuthService authService,
			@Value("${internal.api.token}") String internalToken) {
		this.usuarioRepository = usuarioRepository;
		this.authService = authService;
		this.internalToken = internalToken;
	}

	@PostMapping("/usuarios")
	public ResponseEntity<CrearUsuarioResponse> crearUsuario(
			@RequestBody CrearUsuarioRequest request,
			@RequestHeader(value = "X-Internal-Token", required = false) String token) {
		if (!tokenValido(token)) {
			return ResponseEntity.status(403).build();
		}
		try {
			return ResponseEntity.status(201).body(authService.crearUsuarioInterno(request));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/usuarios/{id}/tipo")
	public ResponseEntity<Map<String, Object>> tipoUsuario(
			@PathVariable Long id,
			@RequestHeader(value = "X-Internal-Token", required = false) String token) {
		if (!tokenValido(token)) {
			return ResponseEntity.status(403).build();
		}
		return usuarioRepository.findById(id)
				.map(u -> ResponseEntity.ok(Map.<String, Object>of("userId", u.getIdUsuario(), "tipo", u.getTipo())))
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/usuarios/{id}/contacto")
	public ResponseEntity<Map<String, Object>> contactoApoderado(
			@PathVariable Long id,
			@RequestHeader(value = "X-Internal-Token", required = false) String token) {
		if (!tokenValido(token)) {
			return ResponseEntity.status(403).build();
		}
		return usuarioRepository.findById(id)
				.map(u -> ResponseEntity.ok(Map.<String, Object>of(
						"userId", u.getIdUsuario(),
						"email", u.getEmail(),
						"telefono", u.getTelefono() != null ? u.getTelefono() : "",
						"nombre", u.getNombre(),
						"apellido", u.getApellido())))
				.orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/usuarios/{id}/sincronizar-demo")
	public ResponseEntity<Void> sincronizarDemo(
			@PathVariable Long id,
			@RequestBody SincronizarDemoRequest request,
			@RequestHeader(value = "X-Internal-Token", required = false) String token) {
		if (!tokenValido(token)) {
			return ResponseEntity.status(403).build();
		}
		try {
			authService.sincronizarDemoInterno(id, request);
			return ResponseEntity.noContent().build();
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/usuarios/by-email/{email}")
	public ResponseEntity<Map<String, Object>> porEmail(
			@PathVariable String email,
			@RequestHeader(value = "X-Internal-Token", required = false) String token) {
		if (!tokenValido(token)) {
			return ResponseEntity.status(403).build();
		}
		return usuarioRepository.findByEmail(email.trim().toLowerCase())
				.map(u -> ResponseEntity.ok(Map.<String, Object>of(
						"id", u.getIdUsuario(),
						"userId", u.getIdUsuario(),
						"email", u.getEmail(),
						"tipo", u.getTipo())))
				.orElse(ResponseEntity.notFound().build());
	}

	private boolean tokenValido(String token) {
		return token != null && token.equals(internalToken);
	}
}
