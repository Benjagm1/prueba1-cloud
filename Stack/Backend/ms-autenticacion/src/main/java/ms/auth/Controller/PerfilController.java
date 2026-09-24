package ms.auth.Controller;

import jakarta.validation.Valid;
import ms.common.security.MsUserPrincipal;
import ms.dto.ActualizarMiPerfilRequest;
import ms.auth.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ms.dto.MiPerfilResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class PerfilController {

	private final AuthService authService;

	public PerfilController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/perfil")
	public ResponseEntity<MiPerfilResponse> obtenerMiPerfil(@AuthenticationPrincipal MsUserPrincipal principal) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.ok(authService.obtenerMiPerfil(principal.userId()));
	}

	@PutMapping("/perfil")
	public ResponseEntity<String> actualizarMiPerfil(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@Valid @RequestBody ActualizarMiPerfilRequest request) {
		if (principal == null) {
			return ResponseEntity.status(401).body("No autenticado");
		}
		return ResponseEntity.ok(authService.actualizarMiPerfil(principal.userId(), request));
	}
}
