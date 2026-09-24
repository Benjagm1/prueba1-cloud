package ms.administracion.web;

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

@WebMvcTest(ApoderadoPupilosController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApoderadoPupilosControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdministracionDominioService dominio;

	@Test
	void apoderadoListaMisPupilos() throws Exception {
		when(dominio.listarPupilosDeApoderado(5L)).thenReturn(List.of());

		mockMvc.perform(get("/api/apoderados/mis-pupilos").with(msUser(5L, "apo@test.cl", "apoderado")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
