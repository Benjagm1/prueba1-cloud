package ms.administracion.web;

import ms.administracion.client.AuthContactoClient;
import ms.administracion.service.AdministracionDominioService;
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

@WebMvcTest(DocenteConsultasController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocenteConsultasControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdministracionDominioService dominio;

	@MockitoBean
	private AuthContactoClient authContactoClient;

	@Test
	void profesorListaMisAsignaciones() throws Exception {
		when(dominio.listarAsignacionesDeProfesor(1L)).thenReturn(List.of());

		mockMvc.perform(get("/api/docentes/mis-asignaciones").with(msUser(1L, "prof@test.cl", "profesor")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
