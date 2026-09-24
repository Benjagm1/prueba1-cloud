package ms.common.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class OpenApiAutoConfiguration {

	@Bean
	public OpenAPI colegioOpenApi(@Value("${spring.application.name:API}") String serviceName) {
		final String securitySchemeName = "bearer-jwt";
		return new OpenAPI()
				.info(new Info()
						.title("Libro de Clases Digital — " + serviceName)
						.description("API REST del Colegio Bernardo O'Higgins (Coquimbo). "
								+ "Autenticación vía JWT emitido por ms-autenticacion.")
						.version("1.0.0")
						.contact(new Contact().name("Colegio Bernardo O'Higgins")))
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(new Components().addSecuritySchemes(securitySchemeName,
						new SecurityScheme()
								.name(securitySchemeName)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("Token JWT obtenido en POST /auth/login")));
	}
}
