package ms.common.testsupport;

import ms.common.security.MsUserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Post-processor para pruebas @WebMvcTest con {@link MsUserPrincipal}.
 */
public final class MsUserPrincipalRequestPostProcessor {

	private MsUserPrincipalRequestPostProcessor() {
	}

	public static RequestPostProcessor msUser(Long userId, String email, String tipo) {
		MsUserPrincipal principal = new MsUserPrincipal(userId, email, tipo);
		UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken(principal, null, java.util.List.of());
		return request -> {
			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(auth);
			SecurityContextHolder.setContext(context);
			return request;
		};
	}
}
