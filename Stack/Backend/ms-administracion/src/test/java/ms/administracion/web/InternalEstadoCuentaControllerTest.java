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

@WebMvcTest(InternalEstadoCuentaController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "internal.api.token=test-token")
class InternalEstadoCuentaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdministracionDominioService dominio;

	@Test
	void getEstadoCuentaConToken() throws Exception {
		when(dominio.evaluarEstadoCuenta(1L, "alumno"))
				.thenReturn(new AdministracionDominioService.EstadoCuenta(true, "OK"));

		mockMvc.perform(get("/internal/usuarios/1/estado-cuenta")
						.param("tipo", "alumno")
						.header("X-Internal-Token", "test-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.completa").value(true))
				.andExpect(jsonPath("$.detalle").value("OK"));
	}
}
