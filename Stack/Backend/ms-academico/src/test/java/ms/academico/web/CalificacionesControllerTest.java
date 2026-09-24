package ms.academico.web;

import ms.academico.service.CalificacionesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static ms.common.testsupport.MsUserPrincipalRequestPostProcessor.msUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalificacionesController.class)
@AutoConfigureMockMvc(addFilters = false)
class CalificacionesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CalificacionesService calificacionesService;

	@Test
	void alumnoObtieneMisNotas() throws Exception {
		when(calificacionesService.listarNotasAlumno(any(), any(), eq(3L))).thenReturn(List.of());

		mockMvc.perform(get("/api/academico/mis-notas")
						.header("Authorization", "Bearer token")
						.with(msUser(3L, "alumno@test.cl", "alumno")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
