package ms.auth.bootstrap;

import ms.auth.Model.Usuario;
import ms.auth.Repository.UsuarioRepository;
import ms.common.security.Roles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Garantiza en BD la cuenta de administración definida por configuración (correo + contraseña en properties/env).
 * Activar solo donde corresponda ({@code app.admin.enabled=true}); en producción usar secretos reales.
 */
@Component
@Order(10)
@ConditionalOnProperty(name = "app.admin.enabled", havingValue = "true")
public class BootstrapAdminAccountRunner implements ApplicationRunner {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final String adminEmail;
	private final String adminPasswordPlano;

	public BootstrapAdminAccountRunner(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.admin.email}") String adminEmail,
			@Value("${app.admin.password}") String adminPasswordPlano) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.adminEmail = adminEmail.trim().toLowerCase();
		this.adminPasswordPlano = adminPasswordPlano;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (usuarioRepository.findByEmail(adminEmail).isPresent()) {
			return;
		}
		Usuario u = new Usuario("Admin", "Sistema", adminEmail, passwordEncoder.encode(adminPasswordPlano), Roles.ADMIN);
		u.setTelefono("");
		usuarioRepository.save(u);
	}
}
