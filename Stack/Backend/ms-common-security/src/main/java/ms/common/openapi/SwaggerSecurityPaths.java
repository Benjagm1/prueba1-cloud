package ms.common.openapi;

/**
 * Rutas públicas de documentación OpenAPI / Swagger UI.
 */
public final class SwaggerSecurityPaths {

	public static final String[] PUBLIC = {
			"/v3/api-docs",
			"/v3/api-docs/**",
			"/swagger-ui.html",
			"/swagger-ui/**"
	};

	private SwaggerSecurityPaths() {
	}
}
