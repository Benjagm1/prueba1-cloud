package ms.asistencia.web;

import ms.asistencia.realtime.AsistenciaSseHub;
import ms.asistencia.service.AsistenciaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static ms.common.testsupport.MsUserPrincipalRequestPostProcessor.msUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AsistenciaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AsistenciaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AsistenciaService asistenciaService;

	@MockitoBean
	private AsistenciaSseHub sseHub;

	@Test
	void alumnoObtieneMiResumen() throws Exception {
		when(asistenciaService.resumenPropio(any(), any(), any()))
				.thenReturn(new AsistenciaService.ResumenAsistenciaDto(10, 9, 90.0, false));

		mockMvc.perform(get("/api/asistencia/mi-resumen")
						.param("desde", "2025-01-01")
						.param("hasta", "2025-06-01")
						.with(msUser(3L, "alumno@test.cl", "alumno")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.diasRegistrados").value(10))
				.andExpect(jsonPath("$.porcentaje").value(90.0));
	}
}
