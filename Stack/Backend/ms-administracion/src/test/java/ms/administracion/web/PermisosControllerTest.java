package ms.administracion.web;

import ms.administracion.service.AdministracionDominioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static ms.common.testsupport.MsUserPrincipalRequestPostProcessor.msUser;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PermisosController.class)
@AutoConfigureMockMvc(addFilters = false)
class PermisosControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdministracionDominioService dominio;

	@Test
	void alumnoPuedeVerNotasPropias() throws Exception {
		when(dominio.puedeVerNotasDeAlumno(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(3L)))
				.thenReturn(true);

		mockMvc.perform(get("/api/permisos/puedo-ver-notas-de/3")
						.with(msUser(3L, "alumno@test.cl", "alumno")))
				.andExpect(status().isNoContent());
	}
}
