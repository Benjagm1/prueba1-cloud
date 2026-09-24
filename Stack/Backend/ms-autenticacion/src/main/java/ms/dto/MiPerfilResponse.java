package ms.dto;

public record MiPerfilResponse(
		Long idUsuario,
		String nombre,
		String apellido,
		String email,
		String telefono,
		String tipo) {
}
