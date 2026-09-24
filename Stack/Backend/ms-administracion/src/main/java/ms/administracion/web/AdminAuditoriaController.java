package ms.administracion.web;

import java.util.List;

import ms.administracion.model.AuditoriaEvento;
import ms.administracion.service.AuditoriaService;
import ms.common.security.MsUserPrincipal;
import ms.common.security.Roles;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminAuditoriaController {

	private final AuditoriaService auditoriaService;

	public AdminAuditoriaController(AuditoriaService auditoriaService) {
		this.auditoriaService = auditoriaService;
	}

	@GetMapping("/auditoria")
	public List<AuditoriaEvento> listar(
			@AuthenticationPrincipal MsUserPrincipal principal,
			@RequestParam(defaultValue = "200") int limite) {
		requerirAdmin(principal);
		return auditoriaService.listarRecientes(limite);
	}

	private static void requerirAdmin(MsUserPrincipal principal) {
		if (principal == null || !Roles.isAdmin(principal.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administración escolar");
		}
	}
}
