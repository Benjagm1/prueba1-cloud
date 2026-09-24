package ms.conducta.service;

import ms.conducta.client.AdministracionAccesoAlumnoClient;
import ms.conducta.client.AdministracionCatalogoInternoClient;
import ms.conducta.client.AdministracionConductaClient;
import ms.conducta.client.AuthUsuarioInternoClient;
import ms.conducta.client.MensajeriaInternaClient;
import ms.conducta.model.Anotacion;
import ms.conducta.repo.AnotacionRepository;
import ms.common.security.MsUserPrincipal;
import ms.common.security.Roles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ConductaService {

	private final AnotacionRepository anotacionRepository;
	private final AdministracionConductaClient administracionConductaClient;
	private final AdministracionAccesoAlumnoClient administracionAccesoAlumnoClient;
	private final MensajeriaInternaClient mensajeriaInternaClient;
	private final AuthUsuarioInternoClient authUsuarioInternoClient;
	private final AdministracionCatalogoInternoClient catalogoInternoClient;
	private final String internalToken;

	public ConductaService(AnotacionRepository anotacionRepository,
			AdministracionConductaClient administracionConductaClient,
			AdministracionAccesoAlumnoClient administracionAccesoAlumnoClient,
			MensajeriaInternaClient mensajeriaInternaClient,
			AuthUsuarioInternoClient authUsuarioInternoClient,
			AdministracionCatalogoInternoClient catalogoInternoClient,
			@Value("${internal.api.token}") String internalToken) {
		this.anotacionRepository = anotacionRepository;
		this.administracionConductaClient = administracionConductaClient;
		this.administracionAccesoAlumnoClient = administracionAccesoAlumnoClient;
		this.mensajeriaInternaClient = mensajeriaInternaClient;
		this.authUsuarioInternoClient = authUsuarioInternoClient;
		this.catalogoInternoClient = catalogoInternoClient;
		this.internalToken = internalToken;
	}

	public record AnotacionVista(
			Long id,
			Long alumnoUsuarioId,
			Long profesorUsuarioId,
			String profesorNombre,
			Long asignaturaId,
			String asignaturaNombre,
			String texto,
			String tipo,
			java.time.Instant creadoEn) {
	}

	@Transactional
	public Anotacion registrarAnotacion(String authorization, MsUserPrincipal profesor, Long alumnoUsuarioId,
			String tipo, String texto, Long cursoId, Long asignaturaId) {
		if (profesor == null || !Roles.isProfesor(profesor.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo profesores");
		}
		String tipoNorm = "negativa".equalsIgnoreCase(tipo) ? "negativa" : "positiva";
		if (texto == null || texto.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El texto de la anotación es obligatorio");
		}
		administracionConductaClient.assertProfesorPuedeGestionarAlumno(authorization, alumnoUsuarioId);
		Anotacion a = new Anotacion();
		a.setAlumnoUsuarioId(alumnoUsuarioId);
		a.setProfesorUsuarioId(profesor.userId());
		a.setCursoId(cursoId);
		a.setAsignaturaId(asignaturaId);
		a.setTipo(tipoNorm);
		a.setTexto(texto.trim());
		Anotacion guardada = anotacionRepository.save(a);

		List<Long> apoderados = administracionConductaClient.listarApoderadosIds(alumnoUsuarioId, internalToken);
		String titulo = "Nueva anotación de conducta";
		String cuerpo = "Se ha registrado una anotación respecto de su pupilo/a. Revise el libro de clases digital.";
		for (Long apoderadoId : apoderados) {
			mensajeriaInternaClient.crearNotificacion(apoderadoId, titulo, cuerpo);
		}
		return guardada;
	}

	public List<AnotacionVista> listarPorAlumno(String authorization, Long alumnoUsuarioId) {
		administracionAccesoAlumnoClient.assertPuedeVerDatosAlumno(authorization, alumnoUsuarioId);
		return anotacionRepository.findByAlumnoUsuarioIdOrderByCreadoEnDesc(alumnoUsuarioId).stream()
				.map(this::toVista)
				.toList();
	}

	public AnotacionVista toVista(Anotacion a) {
		String asignaturaNombre = a.getAsignaturaId() != null
				? catalogoInternoClient.nombreAsignatura(a.getAsignaturaId())
				: null;
		return new AnotacionVista(
				a.getId(),
				a.getAlumnoUsuarioId(),
				a.getProfesorUsuarioId(),
				authUsuarioInternoClient.nombreCompletoProfesor(a.getProfesorUsuarioId()),
				a.getAsignaturaId(),
				asignaturaNombre,
				a.getTexto(),
				a.getTipo(),
				a.getCreadoEn());
	}
}
