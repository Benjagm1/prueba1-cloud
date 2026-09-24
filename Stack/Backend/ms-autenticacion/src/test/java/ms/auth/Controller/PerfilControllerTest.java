package ms.auth.Controller;

import ms.auth.Service.AuthService;
import ms.dto.MiPerfilResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static ms.common.testsupport.MsUserPrincipalRequestPostProcessor.msUser;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PerfilController.class)
@AutoConfigureMockMvc(addFilters = false)
class PerfilControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@Test
	void sinAuthRetorna401() throws Exception {
		mockMvc.perform(get("/auth/perfil"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void conMsUserRetorna200() throws Exception {
		when(authService.obtenerMiPerfil(1L))
				.thenReturn(new MiPerfilResponse(1L, "Juan", "Pérez", "user@test.cl", "", "profesor"));

		mockMvc.perform(get("/auth/perfil").with(msUser(1L, "user@test.cl", "profesor")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("user@test.cl"))
				.andExpect(jsonPath("$.nombre").value("Juan"));
	}
}
