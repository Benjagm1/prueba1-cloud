package ms.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenParser {

	private final SecretKey signingKey;

	public JwtTokenParser(@Value("${jwt.secret}") String secret) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public MsUserPrincipal parseBearerToken(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			throw new JwtException("Token vacío");
		}
		Claims claims = Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(rawToken)
				.getPayload();
		Long uid = claims.get("uid", Long.class);
		String email = claims.getSubject();
		String tipo = claims.get("tipo", String.class);
		if (uid == null || email == null || tipo == null) {
			throw new JwtException("Claims incompletos");
		}
		return new MsUserPrincipal(uid, email, tipo);
	}
}
