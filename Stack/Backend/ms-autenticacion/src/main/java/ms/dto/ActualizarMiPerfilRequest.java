package ms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * HU6: el usuario autenticado actualiza sus datos de contacto y nombre visible.
 */
public record ActualizarMiPerfilRequest(
		@Size(max = 100)
		String nombre,
		@Size(max = 100)
		String apellido,
		@Email(message = "Formato de correo inválido")
		String email,
		@Size(max = 30)
		String telefono
) {
}
