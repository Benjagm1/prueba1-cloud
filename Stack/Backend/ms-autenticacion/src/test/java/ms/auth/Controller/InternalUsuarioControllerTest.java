package ms.auth.Controller;

import ms.auth.Model.Usuario;
import ms.auth.Repository.UsuarioRepository;
import ms.auth.Service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalUsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "internal.api.token=test-token")
class InternalUsuarioControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	@MockitoBean
	private AuthService authService;

	@Test
	void sinTokenRetorna403() throws Exception {
		mockMvc.perform(get("/internal/usuarios/1/tipo"))
				.andExpect(status().isForbidden());
	}

	@Test
	void conTokenRetorna200() throws Exception {
		Usuario u = new Usuario("Juan", "Pérez", "user@test.cl", "hash", "profesor");
		u.setIdUsuario(1L);
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

		mockMvc.perform(get("/internal/usuarios/1/tipo")
						.header("X-Internal-Token", "test-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.tipo").value("profesor"));
	}
}
