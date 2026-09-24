package ms.mensajeria.web;

import ms.mensajeria.service.NotificacionesService;
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

@WebMvcTest(BandejaController.class)
@AutoConfigureMockMvc(addFilters = false)
class BandejaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NotificacionesService notificacionesService;

	@Test
	void obtieneMisNotificaciones() throws Exception {
		when(notificacionesService.bandeja(1L)).thenReturn(List.of());

		mockMvc.perform(get("/api/mensajes/mis-notificaciones")
						.with(msUser(1L, "user@test.cl", "profesor")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
