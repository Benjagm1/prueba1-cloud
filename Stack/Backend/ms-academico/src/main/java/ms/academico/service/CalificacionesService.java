package ms.academico.service;

import ms.academico.client.AdministracionApoderadosClient;
import ms.academico.client.AdministracionPermisosClient;
import ms.academico.client.MensajeriaInternaClient;
import ms.academico.model.Calificacion;
import ms.academico.repo.CalificacionRepository;
import ms.common.audit.AdministracionAuditClient;
import ms.common.security.MsUserPrincipal;
import ms.common.security.Roles;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CalificacionesService {

	private final CalificacionRepository calificacionRepository;
	private final AdministracionPermisosClient permisosClient;
	private final AdministracionApoderadosClient apoderadosClient;
	private final MensajeriaInternaClient mensajeriaInternaClient;
	private final ObjectProvider<AdministracionAuditClient> auditClient;

	public CalificacionesService(CalificacionRepository calificacionRepository,
			AdministracionPermisosClient permisosClient,
			AdministracionApoderadosClient apoderadosClient,
			MensajeriaInternaClient mensajeriaInternaClient,
			ObjectProvider<AdministracionAuditClient> auditClient) {
		this.calificacionRepository = calificacionRepository;
		this.permisosClient = permisosClient;
		this.apoderadosClient = apoderadosClient;
		this.mensajeriaInternaClient = mensajeriaInternaClient;
		this.auditClient = auditClient;
	}

	public List<Calificacion> listarNotasAlumno(String authorization, MsUserPrincipal viewer, Long alumnoUsuarioId) {
		permisosClient.assertPuedeVerNotasDeAlumno(authorization, alumnoUsuarioId);
		return calificacionRepository.findByAlumnoUsuarioIdOrderByAsignaturaIdAscCreadoEnAsc(alumnoUsuarioId);
	}

	@Transactional
	public Calificacion registrar(String authorization, MsUserPrincipal profesor, Long alumnoUsuarioId, Long cursoId,
			Long asignaturaId, String nombreEvaluacion, BigDecimal nota) {
		if (profesor == null || !Roles.isProfesor(profesor.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo profesores registran notas");
		}
		if (nota.scale() > 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nota admite solo un decimal");
		}
		if (nota.compareTo(new BigDecimal("1.0")) < 0 || nota.compareTo(new BigDecimal("7.0")) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nota debe estar entre 1.0 y 7.0");
		}
		permisosClient.assertProfesorDictaEnCurso(authorization, cursoId, asignaturaId);
		Calificacion c = calificacionRepository
				.findFirstByAlumnoUsuarioIdAndCursoIdAndAsignaturaIdAndNombreEvaluacionOrderByCreadoEnDesc(
						alumnoUsuarioId, cursoId, asignaturaId, nombreEvaluacion)
				.orElseGet(Calificacion::new);
		if (c.getId() == null) {
			c.setAlumnoUsuarioId(alumnoUsuarioId);
			c.setCursoId(cursoId);
			c.setAsignaturaId(asignaturaId);
			c.setProfesorUsuarioId(profesor.userId());
			c.setNombreEvaluacion(nombreEvaluacion);
		}
		c.setNota(nota);
		Calificacion guardada = calificacionRepository.save(c);
		notificarNotaApoderados(alumnoUsuarioId, nombreEvaluacion, nota);
		auditClient.ifAvailable(client -> client.registrarSilencioso(
				"academico",
				"CALIFICACION_REGISTRADA",
				profesor.userId(),
				profesor.tipo(),
				alumnoUsuarioId,
				"Evaluación " + nombreEvaluacion + " nota " + nota + " curso " + cursoId));
		return guardada;
	}

	private void notificarNotaApoderados(Long alumnoUsuarioId, String nombreEvaluacion, BigDecimal nota) {
		String titulo = "Nueva calificación registrada";
		String cuerpo = "Se registró la evaluación \"" + nombreEvaluacion + "\" con nota " + nota
				+ " para su pupilo/a. Revise el libro de clases digital.";
		for (Long apoderadoId : apoderadosClient.listarApoderadosIds(alumnoUsuarioId)) {
			mensajeriaInternaClient.crearNotificacion(apoderadoId, titulo, cuerpo);
		}
	}
}
