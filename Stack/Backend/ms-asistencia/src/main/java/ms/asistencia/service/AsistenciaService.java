package ms.asistencia.service;

import ms.asistencia.client.AdministracionApoderadosClient;
import ms.asistencia.client.AdministracionCatalogoInternoClient;
import ms.asistencia.client.AdministracionPermisosClient;
import ms.asistencia.client.MensajeriaInternaClient;
import ms.asistencia.model.RegistroAsistencia;
import ms.asistencia.realtime.AsistenciaSseHub;
import ms.asistencia.repo.RegistroAsistenciaRepository;
import ms.common.audit.AdministracionAuditClient;
import ms.common.security.MsUserPrincipal;
import ms.common.security.Roles;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AsistenciaService {

	private final RegistroAsistenciaRepository registroAsistenciaRepository;
	private final AdministracionPermisosClient permisosClient;
	private final AdministracionApoderadosClient apoderadosClient;
	private final MensajeriaInternaClient mensajeriaInternaClient;
	private final AsistenciaSseHub sseHub;
	private final ObjectProvider<AdministracionAuditClient> auditClient;
	private final AdministracionCatalogoInternoClient catalogoClient;

	public AsistenciaService(RegistroAsistenciaRepository registroAsistenciaRepository,
			AdministracionPermisosClient permisosClient,
			AdministracionApoderadosClient apoderadosClient,
			MensajeriaInternaClient mensajeriaInternaClient,
			AsistenciaSseHub sseHub,
			ObjectProvider<AdministracionAuditClient> auditClient,
			AdministracionCatalogoInternoClient catalogoClient) {
		this.registroAsistenciaRepository = registroAsistenciaRepository;
		this.permisosClient = permisosClient;
		this.apoderadosClient = apoderadosClient;
		this.mensajeriaInternaClient = mensajeriaInternaClient;
		this.sseHub = sseHub;
		this.auditClient = auditClient;
		this.catalogoClient = catalogoClient;
	}

	public record HistorialAsistenciaItem(
			Long id,
			LocalDate fecha,
			boolean presente,
			Long asignaturaId,
			String asignaturaNombre) {
	}

	public void asegurarAccesoAsistenciaAlumno(String authorization, MsUserPrincipal principal, Long alumnoUsuarioId) {
		permisosClient.assertPuedeVerAsistenciaDeAlumno(authorization, alumnoUsuarioId);
	}

	public List<HistorialAsistenciaItem> historial(String authorization, MsUserPrincipal viewer, Long alumnoUsuarioId,
			LocalDate desde, LocalDate hasta) {
		permisosClient.assertPuedeVerAsistenciaDeAlumno(authorization, alumnoUsuarioId);
		return registroAsistenciaRepository
				.findByAlumnoUsuarioIdAndFechaBetweenOrderByFechaAsc(alumnoUsuarioId, desde, hasta)
				.stream()
				.map(r -> new HistorialAsistenciaItem(
						r.getId(),
						r.getFecha(),
						r.isPresente(),
						r.getAsignaturaId(),
						catalogoClient.nombreAsignatura(r.getAsignaturaId())))
				.sorted(Comparator.comparing(HistorialAsistenciaItem::fecha).reversed()
						.thenComparing(HistorialAsistenciaItem::asignaturaNombre))
				.toList();
	}

	public ResumenAsistenciaDto resumenPropio(MsUserPrincipal alumno, LocalDate desde, LocalDate hasta) {
		if (alumno == null || !(esAlumno(alumno))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo alumnos");
		}
		long total = registroAsistenciaRepository.countByAlumnoUsuarioIdAndFechaBetween(alumno.userId(), desde, hasta);
		long presentes = registroAsistenciaRepository.countByAlumnoUsuarioIdAndPresenteIsTrueAndFechaBetween(
				alumno.userId(), desde, hasta);
		double pct = total == 0 ? 100.0 : (presentes * 100.0) / total;
		return new ResumenAsistenciaDto(total, presentes, pct, pct < 85.0);
	}

	@Transactional
	public void guardarDia(String authorization, MsUserPrincipal profesor, LocalDate fecha, Long cursoId,
			Long asignaturaId, List<FilaAsistencia> filas) {
		if (profesor == null || !Roles.isProfesor(profesor.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo profesores");
		}
		permisosClient.assertProfesorDictaEnCurso(authorization, cursoId, asignaturaId);
		for (FilaAsistencia fila : filas) {
			Optional<RegistroAsistencia> existente = registroAsistenciaRepository
					.findByFechaAndAlumnoUsuarioIdAndAsignaturaId(fecha, fila.alumnoUsuarioId(), asignaturaId);
			boolean eraPresente = existente.map(RegistroAsistencia::isPresente).orElse(true);
			RegistroAsistencia r = existente.orElseGet(RegistroAsistencia::new);
			r.setFecha(fecha);
			r.setAlumnoUsuarioId(fila.alumnoUsuarioId());
			r.setCursoId(cursoId);
			r.setAsignaturaId(asignaturaId);
			r.setPresente(fila.presente());
			r.setProfesorUsuarioId(profesor.userId());
			registroAsistenciaRepository.save(r);
			sseHub.publicarActualizacion(fila.alumnoUsuarioId());
			if (!fila.presente() && (existente.isEmpty() || eraPresente)) {
				notificarAusenciaApoderados(fila.alumnoUsuarioId(), fecha);
			}
		}
		auditClient.ifAvailable(client -> client.registrarSilencioso(
				"asistencia",
				"ASISTENCIA_DIA_GUARDADA",
				profesor.userId(),
				profesor.tipo(),
				cursoId,
				"Fecha " + fecha + " asignatura " + asignaturaId + " filas " + filas.size()));
	}

	public List<RegistroDiaDto> consultarDia(String authorization, MsUserPrincipal profesor, LocalDate fecha,
			Long cursoId, Long asignaturaId) {
		if (profesor == null || !Roles.isProfesor(profesor.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo profesores");
		}
		permisosClient.assertProfesorDictaEnCurso(authorization, cursoId, asignaturaId);
		return registroAsistenciaRepository.findByFechaAndCursoIdAndAsignaturaIdOrderByAlumnoUsuarioIdAsc(fecha, cursoId,
				asignaturaId).stream()
				.map(r -> new RegistroDiaDto(r.getAlumnoUsuarioId(), r.isPresente()))
				.toList();
	}

	public record RegistroDiaDto(Long alumnoUsuarioId, boolean presente) {
	}

	public record FilaAsistencia(Long alumnoUsuarioId, boolean presente) {
	}

	public record ResumenAsistenciaDto(long diasRegistrados, long diasPresentes, double porcentaje,
			boolean bajoUmbral85) {
	}

	public ResumenAsistenciaDto resumenDocente(MsUserPrincipal profesor, LocalDate desde, LocalDate hasta) {
		if (profesor == null || !Roles.isProfesor(profesor.tipo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo profesores");
		}
		long total = registroAsistenciaRepository.countByProfesorUsuarioIdAndFechaBetween(profesor.userId(), desde,
				hasta);
		long presentes = registroAsistenciaRepository.countByProfesorUsuarioIdAndPresenteIsTrueAndFechaBetween(
				profesor.userId(), desde, hasta);
		double pct = total == 0 ? 0.0 : (presentes * 100.0) / total;
		return new ResumenAsistenciaDto(total, presentes, pct, pct < 85.0 && total > 0);
	}

	private static boolean esAlumno(MsUserPrincipal viewer) {
		return Roles.isAlumno(viewer.tipo());
	}

	private void notificarAusenciaApoderados(Long alumnoUsuarioId, LocalDate fecha) {
		String titulo = "Ausencia registrada";
		String cuerpo = "Se registró una ausencia de su pupilo/a el día " + fecha
				+ ". Revise el libro de clases digital.";
		for (Long apoderadoId : apoderadosClient.listarApoderadosIds(alumnoUsuarioId)) {
			mensajeriaInternaClient.crearNotificacion(apoderadoId, titulo, cuerpo);
		}
	}
}
