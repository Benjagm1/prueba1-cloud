package ms.administracion.web;

import ms.administracion.repo.ApoderadoAlumnoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalApoderadosController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "internal.api.token=test-token")
class InternalApoderadosControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ApoderadoAlumnoRepository apoderadoAlumnoRepository;

	@Test
	void conTokenRetorna200() throws Exception {
		when(apoderadoAlumnoRepository.findByAlumnoUsuarioId(10L)).thenReturn(List.of());

		mockMvc.perform(get("/internal/alumnos/10/apoderados-ids")
						.header("X-Internal-Token", "test-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
