package ms.mensajeria.web;

import ms.mensajeria.service.MensajesDirectosService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static ms.common.testsupport.MsUserPrincipalRequestPostProcessor.msUser;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MensajesDirectosController.class)
@AutoConfigureMockMvc(addFilters = false)
class MensajesDirectosControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MensajesDirectosService mensajesDirectosService;

	@Test
	void obtieneRecibidos() throws Exception {
		when(mensajesDirectosService.recibidos(1L)).thenReturn(List.of());

		mockMvc.perform(get("/api/mensajes/recibidos")
						.with(msUser(1L, "user@test.cl", "profesor")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
