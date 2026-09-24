package ms.asistencia.config;

import ms.common.openapi.SwaggerSecurityPaths;
import ms.common.security.JwtAuthenticationFilter;
import ms.common.security.JwtTokenParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class AsistenciaSecurityConfig {

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenParser jwtTokenParser) {
		return new JwtAuthenticationFilter(jwtTokenParser);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
			throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(SwaggerSecurityPaths.PUBLIC).permitAll()
						.requestMatchers("/actuator/**").permitAll()
						.anyRequest().authenticated());
		return http.build();
	}
}
