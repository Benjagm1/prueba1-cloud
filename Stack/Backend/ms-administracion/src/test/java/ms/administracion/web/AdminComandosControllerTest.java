package ms.administracion.web;

import ms.administracion.service.AdministracionDominioService;
import ms.administracion.service.AuditoriaService;
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

@WebMvcTest(AdminComandosController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminComandosControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdministracionDominioService dominio;

	@MockitoBean
	private AuditoriaService auditoriaService;

	@Test
	void adminListaCursos() throws Exception {
		when(dominio.listarCursos()).thenReturn(List.of());

		mockMvc.perform(get("/api/admin/catalogo/cursos").with(msUser(1L, "admin@test.cl", "admin")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
