package ms.administracion.web;

import ms.administracion.model.Asignatura;
import ms.administracion.repo.AsignaturaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalCatalogoController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "internal.api.token=test-token")
class InternalCatalogoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AsignaturaRepository asignaturaRepository;

	@Test
	void getAsignaturaConToken() throws Exception {
		Asignatura a = mock(Asignatura.class);
		when(a.getId()).thenReturn(1L);
		when(a.getCodigo()).thenReturn("MAT");
		when(a.getNombre()).thenReturn("Matemáticas");
		when(asignaturaRepository.findById(1L)).thenReturn(Optional.of(a));

		mockMvc.perform(get("/internal/asignaturas/1")
						.header("X-Internal-Token", "test-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.codigo").value("MAT"))
				.andExpect(jsonPath("$.nombre").value("Matemáticas"));
	}
}
