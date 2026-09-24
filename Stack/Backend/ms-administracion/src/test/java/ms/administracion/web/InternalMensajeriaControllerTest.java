package ms.administracion.web;

import ms.administracion.service.AdministracionDominioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalMensajeriaController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "internal.api.token=test-token")
class InternalMensajeriaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdministracionDominioService dominio;

	@Test
	void puedeEnviarConToken() throws Exception {
		when(dominio.puedeEnviarMensaje(1L, "profesor", 2L, "alumno")).thenReturn(true);

		mockMvc.perform(get("/internal/mensajes/puede-enviar")
						.param("de", "1")
						.param("deTipo", "profesor")
						.param("para", "2")
						.param("paraTipo", "alumno")
						.header("X-Internal-Token", "test-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.permitido").value(true));
	}
}
