package ms.auth.Controller;

import ms.auth.Service.AuthService;
import ms.dto.AuthResponse;
import ms.dto.LoginRequest;
import ms.dto.RegistroRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@Test
	void loginOk() throws Exception {
		when(authService.login(any(LoginRequest.class)))
				.thenReturn(new AuthResponse("token-abc", 1L, "user@test.cl", "profesor"));

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@test.cl","contrasena":"secret"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("token-abc"))
				.andExpect(jsonPath("$.email").value("user@test.cl"));
	}

	@Test
	void registrarForbidden() throws Exception {
		when(authService.registrar(any(RegistroRequest.class)))
				.thenThrow(new RuntimeException("Registro público deshabilitado"));

		mockMvc.perform(post("/auth/registrar")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre":"A","apellido":"B","email":"x@test.cl","contrasena":"123","tipo":"alumno"}
								"""))
				.andExpect(status().isForbidden());
	}
}
