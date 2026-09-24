package ms.dto;

public record RegistroRequest(
    String nombre,
    String apellido,
    String email,
    String contrasena,
    String tipo // alumno | apoderado | profesor  (sin admin; "estudiante" se normaliza a alumno)
) {}
