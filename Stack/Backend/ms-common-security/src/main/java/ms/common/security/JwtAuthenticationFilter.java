package ms.common.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Coloca {@link MsUserPrincipal} en el contexto si el encabezado Authorization trae un JWT válido.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenParser jwtTokenParser;

	public JwtAuthenticationFilter(JwtTokenParser jwtTokenParser) {
		this.jwtTokenParser = jwtTokenParser;
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
			String token = header.substring(7).trim();
			try {
				MsUserPrincipal principal = jwtTokenParser.parseBearerToken(token);
				String role = "ROLE_" + principal.tipo().toUpperCase();
				var auth = new UsernamePasswordAuthenticationToken(
						principal,
						null,
						List.of(new SimpleGrantedAuthority(role)));
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (JwtException ignored) {
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}
}
