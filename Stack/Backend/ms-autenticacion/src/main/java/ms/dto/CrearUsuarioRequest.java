package ms.dto;

public record CrearUsuarioRequest(
		String nombre,
		String apellido,
		String email,
		String contrasena,
		String tipo) {
}
