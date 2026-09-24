package ms.administracion.web;

import ms.administracion.service.AdministracionDominioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static ms.common.testsupport.MsUserPrincipalRequestPostProcessor.msUser;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlumnoConsultasController.class)
@AutoConfigureMockMvc(addFilters = false)
class AlumnoConsultasControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdministracionDominioService dominio;

	@Test
	void alumnoObtieneMiFicha() throws Exception {
		var ficha = new AdministracionDominioService.AlumnoFichaResumen(
				3L, "Ana", "López", 1L, "3° A", "3A", List.of());
		when(dominio.fichaAlumno(3L)).thenReturn(Optional.of(ficha));

		mockMvc.perform(get("/api/alumnos/mi-ficha").with(msUser(3L, "alumno@test.cl", "alumno")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Ana"))
				.andExpect(jsonPath("$.cursoNombre").value("3° A"));
	}
}
