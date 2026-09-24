package ms.administracion.web;

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

@WebMvcTest(AdminAuditoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAuditoriaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuditoriaService auditoriaService;

	@Test
	void adminListaAuditoria() throws Exception {
		when(auditoriaService.listarRecientes(200)).thenReturn(List.of());

		mockMvc.perform(get("/api/admin/auditoria").with(msUser(1L, "admin@test.cl", "admin")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
