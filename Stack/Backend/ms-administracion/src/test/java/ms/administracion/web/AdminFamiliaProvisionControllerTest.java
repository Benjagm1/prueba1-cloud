package ms.administracion.web;

import ms.administracion.client.AuthUsuarioClient;
import ms.administracion.service.AdministracionDominioService;
import ms.administracion.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static ms.common.testsupport.MsUserPrincipalRequestPostProcessor.msUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminFamiliaProvisionController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminFamiliaProvisionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthUsuarioClient authUsuarioClient;

	@MockitoBean
	private AdministracionDominioService dominio;

	@MockitoBean
	private AuditoriaService auditoriaService;

	@Test
	void postRetorna403ParaNoAdmin() throws Exception {
		mockMvc.perform(post("/api/admin/cuentas/provisionar-familia")
						.with(msUser(2L, "prof@test.cl", "profesor"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isForbidden());
	}
}
