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

@Component
@Order(1)
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true", matchIfMissing = true)
public class BootstrapProfesorDemoRunner implements ApplicationRunner {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final String demoEmail;
	private final String demoPassword;

	public BootstrapProfesorDemoRunner(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.demo.profesor.email:profesor@boh.cl}") String demoEmail,
			@Value("${app.demo.profesor.password:MiClave123!}") String demoPassword) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.demoEmail = demoEmail.trim().toLowerCase();
		this.demoPassword = demoPassword;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (usuarioRepository.findByEmail(demoEmail).isPresent()) {
			return;
		}
		Usuario u = new Usuario("Ana", "Pérez", demoEmail, passwordEncoder.encode(demoPassword), Roles.PROFESOR);
		u.setTelefono("");
		usuarioRepository.save(u);
	}
}
