package ms.administracion.web;

import ms.administracion.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalAuditoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "internal.api.token=test-token")
class InternalAuditoriaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuditoriaService auditoriaService;

	@Test
	void postConTokenRetorna204() throws Exception {
		mockMvc.perform(post("/internal/auditoria")
						.header("X-Internal-Token", "test-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"modulo":"test","accion":"ACCION","actorUsuarioId":1,"actorTipo":"admin","recursoId":2,"detalle":"ok"}
								"""))
				.andExpect(status().isNoContent());
	}
}
