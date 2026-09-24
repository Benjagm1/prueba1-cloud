package ms.dto;

/**
 * Campos opcionales: solo se aplican los que vengan no nulos en el JSON.
 */
public record ActualizarUsuarioRequest(
    String nombre,
    String apellido,
    String email,
    String telefono,
    String tipo
) {}
