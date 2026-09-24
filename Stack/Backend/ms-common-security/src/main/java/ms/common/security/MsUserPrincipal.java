package ms.common.security;

import java.io.Serializable;

/**
 * Identidad extraída del JWT emitido por MS-Autenticación (claims: subject=email, uid, tipo).
 */
public record MsUserPrincipal(Long userId, String email, String tipo) implements Serializable {
}
