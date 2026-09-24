package ms.conducta.web;

import ms.conducta.service.ConductaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnotacionesController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnotacionesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ConductaService conductaService;

	@Test
	void listarAnotaciones() throws Exception {
		when(conductaService.listarPorAlumno(org.mockito.ArgumentMatchers.any(), eq(3L)))
				.thenReturn(List.of());

		mockMvc.perform(get("/api/conducta/alumnos/3/anotaciones")
						.header("Authorization", "Bearer token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
