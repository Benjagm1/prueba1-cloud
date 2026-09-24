package ms.dto;

public record AuthResponse(
    String token,
    Long userId,
    String email,
    String tipo
) {}
