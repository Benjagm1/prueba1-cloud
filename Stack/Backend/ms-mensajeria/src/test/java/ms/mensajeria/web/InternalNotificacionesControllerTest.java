package ms.mensajeria.web;

import ms.mensajeria.model.Notificacion;
import ms.mensajeria.service.NotificacionesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalNotificacionesController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "internal.api.token=test-token")
class InternalNotificacionesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NotificacionesService notificacionesService;

	@Test
	void crearConTokenRetorna200() throws Exception {
		when(notificacionesService.crearInterno(anyLong(), anyString(), anyString()))
				.thenReturn(new Notificacion());

		mockMvc.perform(post("/internal/notificaciones")
						.header("X-Internal-Token", "test-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"destinatarioUsuarioId":1,"titulo":"Hola","cuerpo":"Mensaje"}
								"""))
				.andExpect(status().isOk());
	}
}
