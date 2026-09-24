package ms.dto;

public record LoginRequest(
    String email,
    String contrasena
) {}