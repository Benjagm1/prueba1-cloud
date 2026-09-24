package ms.auth.Service;

import ms.auth.Model.Usuario;
import ms.auth.Repository.UsuarioRepository;
import ms.auth.client.AdministracionEstadoCuentaClient;
import ms.common.audit.AdministracionAuditClient;
import ms.common.security.Roles;
import ms.dto.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AdministracionEstadoCuentaClient estadoCuentaClient;
	private final ObjectProvider<AdministracionAuditClient> auditClient;
	private final String adminEmail;

	public AuthService(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			AdministracionEstadoCuentaClient estadoCuentaClient,
			ObjectProvider<AdministracionAuditClient> auditClient,
			@Value("${app.admin.email}") String adminEmail) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.estadoCuentaClient = estadoCuentaClient;
		this.auditClient = auditClient;
		this.adminEmail = adminEmail.trim().toLowerCase();
	}

	public CrearUsuarioResponse crearUsuario(CrearUsuarioRequest request) {
		String email = request.email().trim().toLowerCase();
		if (usuarioRepository.findByEmail(email).isPresent()) {
			throw new RuntimeException("El email ya está registrado");
		}
		if (email.equalsIgnoreCase(adminEmail)) {
			throw new RuntimeException("Este correo está reservado para la cuenta de administración");
		}
		String tipo = normalizarTipoRegistro(request.tipo());
		Usuario nuevo = new Usuario(
				request.nombre(),
				request.apellido(),
				email,
				passwordEncoder.encode(request.contrasena()),
				tipo);
		usuarioRepository.save(nuevo);
		return new CrearUsuarioResponse(nuevo.getIdUsuario(), nuevo.getEmail(), nuevo.getTipo());
	}

	public CrearUsuarioResponse crearUsuarioInterno(CrearUsuarioRequest request) {
		return crearUsuario(request);
	}

	public String registrar(RegistroRequest request) {
		throw new RuntimeException("El registro público está deshabilitado. Solo el administrador puede crear cuentas.");
	}

	public AuthResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase();
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

		if (!passwordEncoder.matches(request.contrasena(), usuario.getContrasena())) {
			throw new RuntimeException("Credenciales inválidas");
		}

		if (Roles.isAdmin(usuario.getTipo())) {
			if (!usuario.getEmail().equalsIgnoreCase(adminEmail)) {
				throw new RuntimeException("Credenciales inválidas");
			}
		} else if (usuario.getEmail().equalsIgnoreCase(adminEmail)) {
			throw new RuntimeException("Credenciales inválidas");
		}

		if (usuario.getTipo() == null || usuario.getTipo().isBlank()) {
			throw new RuntimeException("La cuenta no tiene rol asignado. Contacte al administrador.");
		}

		if (!Roles.isAdmin(usuario.getTipo())) {
			validarCuentaCompletaParaLogin(usuario.getIdUsuario(), usuario.getTipo());
		}

		String token = jwtService.generarToken(usuario.getIdUsuario(), usuario.getEmail(), usuario.getTipo());
		auditClient.ifAvailable(client -> client.registrarSilencioso(
				"login", "INICIO_SESION", usuario.getIdUsuario(), usuario.getTipo(), "Acceso exitoso"));
		return new AuthResponse(token, usuario.getIdUsuario(), usuario.getEmail(), usuario.getTipo());
	}

	public MiPerfilResponse obtenerMiPerfil(Long userId) {
		Usuario usuario = usuarioRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		return new MiPerfilResponse(
				usuario.getIdUsuario(),
				usuario.getNombre(),
				usuario.getApellido(),
				usuario.getEmail(),
				usuario.getTelefono(),
				usuario.getTipo());
	}

	public List<Usuario> obtenerUsuarios() {
		return usuarioRepository.findAll();
	}

	public String eliminarUsuario(Long id) {
		if (!usuarioRepository.existsById(id)) {
			throw new RuntimeException("Usuario no encontrado con el ID: " + id);
		}
		usuarioRepository.deleteById(id);
		return "Usuario eliminado correctamente";
	}

	public String actualizarMiPerfil(Long userId, ActualizarMiPerfilRequest request) {
		Usuario usuario = usuarioRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		if (Roles.isAdmin(usuario.getTipo()) && request.email() != null
				&& !request.email().trim().equalsIgnoreCase(adminEmail)) {
			throw new RuntimeException("El correo del administrador no puede modificarse");
		}

		if (request.email() != null && !request.email().trim().equalsIgnoreCase(usuario.getEmail())) {
			String nuevo = request.email().trim().toLowerCase();
			if (usuarioRepository.findByEmail(nuevo).isPresent()) {
				throw new RuntimeException("El correo ya está en uso");
			}
			if (nuevo.equalsIgnoreCase(adminEmail)) {
				throw new RuntimeException("Este correo está reservado");
			}
			usuario.setEmail(nuevo);
		}
		if (request.telefono() != null) {
			usuario.setTelefono(request.telefono());
		}
		if (request.nombre() != null && !request.nombre().isBlank() && !Roles.isAdmin(usuario.getTipo())) {
			usuario.setNombre(request.nombre().trim());
		}
		if (request.apellido() != null && !request.apellido().isBlank() && !Roles.isAdmin(usuario.getTipo())) {
			usuario.setApellido(request.apellido().trim());
		}
		usuarioRepository.save(usuario);
		return "Datos actualizados correctamente";
	}

	public String actualizarUsuario(Long id, ActualizarUsuarioRequest request) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado con el ID: " + id));

		if (request.tipo() != null && Roles.isAdmin(request.tipo())) {
			throw new RuntimeException("No se puede asignar el rol admin por esta vía");
		}

		if (Roles.isAdmin(usuario.getTipo())) {
			if (request.email() != null && !request.email().trim().equalsIgnoreCase(adminEmail)) {
				throw new RuntimeException("El correo del administrador no puede modificarse");
			}
			if (request.tipo() != null && !Roles.isAdmin(request.tipo())) {
				throw new RuntimeException("No se puede quitar el rol admin a la cuenta de administración");
			}
		}

		if (request.email() != null && !request.email().trim().equalsIgnoreCase(usuario.getEmail())) {
			String nuevo = request.email().trim().toLowerCase();
			if (usuarioRepository.findByEmail(nuevo).isPresent()) {
				throw new RuntimeException("El nuevo correo ya está en uso por otra cuenta");
			}
			if (nuevo.equalsIgnoreCase(adminEmail)) {
				throw new RuntimeException("Este correo está reservado");
			}
			usuario.setEmail(nuevo);
		}
		if (request.nombre() != null) {
			usuario.setNombre(request.nombre());
		}
		if (request.apellido() != null) {
			usuario.setApellido(request.apellido());
		}
		if (request.telefono() != null) {
			usuario.setTelefono(request.telefono());
		}
		if (request.tipo() != null) {
			usuario.setTipo(request.tipo().trim().toLowerCase());
		}

		usuarioRepository.save(usuario);
		return "Usuario actualizado correctamente";
	}

	public void sincronizarDemoInterno(Long id, SincronizarDemoRequest request) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		if (Roles.isAdmin(usuario.getTipo())) {
			throw new RuntimeException("No se puede sincronizar la cuenta admin por esta vía");
		}
		if (request.nombre() != null && !request.nombre().isBlank()) {
			usuario.setNombre(request.nombre().trim());
		}
		if (request.apellido() != null && !request.apellido().isBlank()) {
			usuario.setApellido(request.apellido().trim());
		}
		if (request.contrasena() != null && !request.contrasena().isBlank()) {
			usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
		}
		if (request.email() != null && !request.email().isBlank()) {
			String nuevo = request.email().trim().toLowerCase();
			if (!nuevo.equalsIgnoreCase(usuario.getEmail())
					&& usuarioRepository.findByEmail(nuevo).isEmpty()) {
				usuario.setEmail(nuevo);
			}
		}
		usuarioRepository.save(usuario);
	}

	private void validarCuentaCompletaParaLogin(Long usuarioId, String tipo) {
		try {
			Map<String, Object> estado = estadoCuentaClient.estadoCuenta(usuarioId, tipo);
			if (estado == null) {
				throw new RuntimeException("No se pudo verificar el estado de la cuenta");
			}
			Object completa = estado.get("completa");
			if (Boolean.FALSE.equals(completa) || completa == null) {
				String detalle = estado.get("detalle") != null ? estado.get("detalle").toString()
						: "La cuenta aún no está configurada por el administrador";
				throw new RuntimeException(detalle);
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("No se pudo verificar el estado de la cuenta. Intente más tarde.");
		}
	}

	private String normalizarTipoRegistro(String tipo) {
		if (tipo == null || tipo.isBlank()) {
			throw new RuntimeException("Debe indicar un rol: alumno, apoderado o profesor");
		}
		String t = tipo.trim().toLowerCase();
		if (Roles.isAdmin(t)) {
			throw new RuntimeException("El rol admin no puede crearse por registro público");
		}
		if ("estudiante".equals(t)) {
			return Roles.ALUMNO;
		}
		if (Roles.isAlumno(t) || Roles.isApoderado(t) || Roles.isProfesor(t)) {
			return t;
		}
		throw new RuntimeException("Rol no válido. Use: alumno, apoderado o profesor");
	}
}
