package ms.administracion.service;

import ms.administracion.client.AuthContactoClient;
import ms.administracion.client.AuthUsuarioClient;
import ms.administracion.model.AlumnoCurso;
import ms.administracion.model.Asignatura;
import ms.administracion.model.ApoderadoAlumno;
import ms.administracion.model.AuditoriaContactoApoderado;
import ms.administracion.model.Curso;
import ms.administracion.model.DocenteAsignacion;
import ms.administracion.repo.AlumnoCursoRepository;
import ms.administracion.repo.AsignaturaRepository;
import ms.administracion.repo.ApoderadoAlumnoRepository;
import ms.administracion.repo.AuditoriaContactoApoderadoRepository;
import ms.administracion.repo.CursoRepository;
import ms.administracion.repo.DocenteAsignacionRepository;
import ms.common.security.MsUserPrincipal;
import ms.common.security.Roles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdministracionDominioService {

	private final CursoRepository cursoRepository;
	private final AsignaturaRepository asignaturaRepository;
	private final AlumnoCursoRepository alumnoCursoRepository;
	private final ApoderadoAlumnoRepository apoderadoAlumnoRepository;
	private final DocenteAsignacionRepository docenteAsignacionRepository;
	private final AuditoriaContactoApoderadoRepository auditoriaContactoApoderadoRepository;
	private final AuthContactoClient authContactoClient;
	private final AuthUsuarioClient authUsuarioClient;

	public AdministracionDominioService(
			CursoRepository cursoRepository,
			AsignaturaRepository asignaturaRepository,
			AlumnoCursoRepository alumnoCursoRepository,
			ApoderadoAlumnoRepository apoderadoAlumnoRepository,
			DocenteAsignacionRepository docenteAsignacionRepository,
			AuditoriaContactoApoderadoRepository auditoriaContactoApoderadoRepository,
			AuthContactoClient authContactoClient,
			AuthUsuarioClient authUsuarioClient) {
		this.cursoRepository = cursoRepository;
		this.asignaturaRepository = asignaturaRepository;
		this.alumnoCursoRepository = alumnoCursoRepository;
		this.apoderadoAlumnoRepository = apoderadoAlumnoRepository;
		this.docenteAsignacionRepository = docenteAsignacionRepository;
		this.auditoriaContactoApoderadoRepository = auditoriaContactoApoderadoRepository;
		this.authContactoClient = authContactoClient;
		this.authUsuarioClient = authUsuarioClient;
	}

	public List<Curso> listarCursos() {
		return cursoRepository.findAll().stream().sorted((a, b) -> Integer.compare(a.getOrden(), b.getOrden())).toList();
	}

	public List<Asignatura> listarAsignaturas() {
		return asignaturaRepository.findAll();
	}

	@Transactional
	public void inscribirAlumnoEnCurso(Long alumnoUsuarioId, Long cursoId, String nombre, String apellido) {
		if (alumnoCursoRepository.findByAlumnoUsuarioId(alumnoUsuarioId).isPresent()) {
			throw new IllegalStateException("El alumno ya tiene un curso asignado");
		}
		Curso curso = cursoRepository.findById(cursoId).orElseThrow(() -> new IllegalArgumentException("Curso no existe"));
		if (curso.getCodigo() == null || !curso.getCodigo().startsWith("MED")) {
			throw new IllegalArgumentException(
					"Por ahora solo se inscriben alumnos en cursos de enseñanza media (MED1–MED4)");
		}
		AlumnoCurso ac = new AlumnoCurso();
		ac.setAlumnoUsuarioId(alumnoUsuarioId);
		ac.setCurso(curso);
		ac.setNombre(nombre);
		ac.setApellido(apellido);
		alumnoCursoRepository.save(ac);
	}

	@Transactional
	public void vincularApoderadoAlumno(Long apoderadoUsuarioId, Long alumnoUsuarioId) {
		if (apoderadoAlumnoRepository.existsByApoderadoUsuarioIdAndAlumnoUsuarioId(apoderadoUsuarioId, alumnoUsuarioId)) {
			return;
		}
		ApoderadoAlumno v = new ApoderadoAlumno();
		v.setApoderadoUsuarioId(apoderadoUsuarioId);
		v.setAlumnoUsuarioId(alumnoUsuarioId);
		apoderadoAlumnoRepository.save(v);
	}

	/** Demo seed: garantiza un único apoderado correcto por alumno (reemplaza vínculos obsoletos). */
	@Transactional
	public void reconciliarVinculoApoderadoAlumno(Long apoderadoUsuarioId, Long alumnoUsuarioId) {
		List<ApoderadoAlumno> existentes = apoderadoAlumnoRepository.findByAlumnoUsuarioId(alumnoUsuarioId);
		boolean yaCorrecto = existentes.size() == 1
				&& existentes.get(0).getApoderadoUsuarioId().equals(apoderadoUsuarioId);
		if (yaCorrecto) {
			return;
		}
		apoderadoAlumnoRepository.deleteAll(existentes);
		vincularApoderadoAlumno(apoderadoUsuarioId, alumnoUsuarioId);
	}

	@Transactional
	public void asignarDocenteAsignaturaCurso(Long profesorUsuarioId, Long asignaturaId, Long cursoId) {
		if (docenteAsignacionRepository.existsByProfesorUsuarioIdAndAsignatura_IdAndCurso_Id(profesorUsuarioId, asignaturaId, cursoId)) {
			return;
		}
		Asignatura asignatura = asignaturaRepository.findById(asignaturaId).orElseThrow(() -> new IllegalArgumentException("Asignatura no existe"));
		Curso curso = cursoRepository.findById(cursoId).orElseThrow(() -> new IllegalArgumentException("Curso no existe"));
		DocenteAsignacion d = new DocenteAsignacion();
		d.setProfesorUsuarioId(profesorUsuarioId);
		d.setAsignatura(asignatura);
		d.setCurso(curso);
		docenteAsignacionRepository.save(d);
	}

	public boolean puedeVerNotasDeAlumno(MsUserPrincipal viewer, Long alumnoUsuarioId) {
		if (esAdministrador(viewer)) {
			return true;
		}
		if (Roles.isProfesor(viewer.tipo())) {
			return profesorTieneAlumnoEnSuCurso(viewer.userId(), alumnoUsuarioId);
		}
		if (esAlumno(viewer)) {
			return viewer.userId().equals(alumnoUsuarioId);
		}
		if (esApoderado(viewer)) {
			return apoderadoAlumnoRepository.existsByApoderadoUsuarioIdAndAlumnoUsuarioId(viewer.userId(), alumnoUsuarioId);
		}
		return false;
	}

	public boolean profesorDictaAsignaturaEnCurso(Long profesorUsuarioId, Long asignaturaId, Long cursoId) {
		return docenteAsignacionRepository.existsByProfesorUsuarioIdAndAsignatura_IdAndCurso_Id(profesorUsuarioId, asignaturaId, cursoId);
	}

	public Optional<Long> cursoIdDelAlumno(Long alumnoUsuarioId) {
		return alumnoCursoRepository.findByAlumnoUsuarioId(alumnoUsuarioId).map(ac -> ac.getCurso().getId());
	}

	public boolean profesorTieneAlumnoEnSuCurso(Long profesorUsuarioId, Long alumnoUsuarioId) {
		Optional<AlumnoCurso> ac = alumnoCursoRepository.findByAlumnoUsuarioId(alumnoUsuarioId);
		if (ac.isEmpty()) {
			return false;
		}
		Long cursoId = ac.get().getCurso().getId();
		return docenteAsignacionRepository.existsByProfesorUsuarioIdAndCurso_Id(profesorUsuarioId, cursoId);
	}

	public List<AlumnoCurso> listaAlumnosCursoOrdenada(Long cursoId) {
		return alumnoCursoRepository.findByCurso_IdOrderByApellidoAscNombreAsc(cursoId);
	}

	public List<ApoderadoAlumno> apoderadosDelAlumno(Long alumnoUsuarioId) {
		return apoderadoAlumnoRepository.findByAlumnoUsuarioId(alumnoUsuarioId);
	}

	public record PupiloResumen(Long alumnoUsuarioId, String nombre, String apellido) {
	}

	public List<PupiloResumen> listarPupilosDeApoderado(Long apoderadoUsuarioId) {
		return apoderadoAlumnoRepository.findByApoderadoUsuarioId(apoderadoUsuarioId).stream()
				.map(v -> alumnoCursoRepository.findByAlumnoUsuarioId(v.getAlumnoUsuarioId()))
				.filter(java.util.Optional::isPresent)
				.map(opt -> {
					AlumnoCurso ac = opt.get();
					return new PupiloResumen(ac.getAlumnoUsuarioId(), ac.getNombre(), ac.getApellido());
				})
				.toList();
	}

	public record AsignacionDocenteResumen(Long cursoId, String cursoNombre, Long asignaturaId, String asignaturaNombre) {
	}

	public List<AsignacionDocenteResumen> listarAsignacionesDeProfesor(Long profesorUsuarioId) {
		return docenteAsignacionRepository.findByProfesorUsuarioId(profesorUsuarioId).stream()
				.map(d -> new AsignacionDocenteResumen(
						d.getCurso().getId(),
						d.getCurso().getNombre(),
						d.getAsignatura().getId(),
						d.getAsignatura().getNombre()))
				.toList();
	}

	public record ContactoMensajeria(Long usuarioId, String nombre, String apellido, String email, String tipo,
			String contexto) {
	}

	public List<ContactoMensajeria> contactosMensajeriaProfesor(Long profesorUsuarioId) {
		LinkedHashMap<Long, ContactoMensajeria> unicos = new LinkedHashMap<>();
		for (AsignacionDocenteResumen asig : listarAsignacionesDeProfesor(profesorUsuarioId)) {
			String base = asig.cursoNombre() + " · " + asig.asignaturaNombre();
			for (AlumnoCurso ac : listaAlumnosCursoOrdenada(asig.cursoId())) {
				agregarContacto(unicos, ac.getAlumnoUsuarioId(), ac.getNombre(), ac.getApellido(), null, Roles.ALUMNO,
						base + " · Alumno");
				for (ApoderadoAlumno v : apoderadosDelAlumno(ac.getAlumnoUsuarioId())) {
					agregarContactoAuth(unicos, v.getApoderadoUsuarioId(),
							base + " · Apoderado de " + ac.getNombre(), Roles.APODERADO);
				}
			}
		}
		return new ArrayList<>(unicos.values());
	}

	public List<ContactoMensajeria> contactosMensajeriaApoderado(Long apoderadoUsuarioId) {
		LinkedHashMap<Long, ContactoMensajeria> unicos = new LinkedHashMap<>();
		for (PupiloResumen pupilo : listarPupilosDeApoderado(apoderadoUsuarioId)) {
			Optional<Long> cursoId = cursoIdDelAlumno(pupilo.alumnoUsuarioId());
			if (cursoId.isEmpty()) {
				continue;
			}
			for (DocenteAsignacion d : docenteAsignacionRepository.findByCurso_Id(cursoId.get())) {
				String ctx = d.getCurso().getNombre() + " · " + d.getAsignatura().getNombre() + " · Profesor de "
						+ pupilo.nombre();
				agregarContactoAuth(unicos, d.getProfesorUsuarioId(), ctx, Roles.PROFESOR);
			}
		}
		return new ArrayList<>(unicos.values());
	}

	public List<ContactoMensajeria> contactosMensajeriaAlumno(Long alumnoUsuarioId) {
		LinkedHashMap<Long, ContactoMensajeria> unicos = new LinkedHashMap<>();
		Optional<Long> cursoId = cursoIdDelAlumno(alumnoUsuarioId);
		if (cursoId.isEmpty()) {
			return List.of();
		}
		for (DocenteAsignacion d : docenteAsignacionRepository.findByCurso_Id(cursoId.get())) {
			String ctx = d.getCurso().getNombre() + " · " + d.getAsignatura().getNombre();
			agregarContactoAuth(unicos, d.getProfesorUsuarioId(), ctx, Roles.PROFESOR);
		}
		return new ArrayList<>(unicos.values());
	}

	public record AsignaturaResumen(Long id, String nombre, String codigo) {
	}

	public record AlumnoFichaResumen(
			Long alumnoUsuarioId,
			String nombre,
			String apellido,
			Long cursoId,
			String cursoNombre,
			String cursoCodigo,
			List<AsignaturaResumen> asignaturas) {
	}

	public record ApoderadoContactoResumen(
			Long apoderadoUsuarioId,
			String nombre,
			String apellido,
			String email,
			String telefono) {
	}

	public Optional<ApoderadoContactoResumen> apoderadoVinculadoAlumno(Long alumnoUsuarioId) {
		List<ApoderadoAlumno> vinculos = apoderadoAlumnoRepository.findByAlumnoUsuarioId(alumnoUsuarioId);
		if (vinculos.isEmpty()) {
			return Optional.empty();
		}
		Long apoderadoId = vinculos.get(0).getApoderadoUsuarioId();
		Map<String, Object> contacto = authContactoClient.obtenerContactoUsuario(apoderadoId);
		if (contacto == null) {
			return Optional.empty();
		}
		return Optional.of(new ApoderadoContactoResumen(
				apoderadoId,
				String.valueOf(contacto.getOrDefault("nombre", "")),
				String.valueOf(contacto.getOrDefault("apellido", "")),
				String.valueOf(contacto.getOrDefault("email", "")),
				String.valueOf(contacto.getOrDefault("telefono", ""))));
	}

	public Optional<AlumnoFichaResumen> fichaAlumno(Long alumnoUsuarioId) {
		return alumnoCursoRepository.findByAlumnoUsuarioId(alumnoUsuarioId).map(ac -> {
			LinkedHashMap<Long, AsignaturaResumen> asigMap = new LinkedHashMap<>();
			for (DocenteAsignacion d : docenteAsignacionRepository.findByCurso_Id(ac.getCurso().getId())) {
				Long asigId = d.getAsignatura().getId();
				asigMap.putIfAbsent(asigId, new AsignaturaResumen(
						asigId,
						d.getAsignatura().getNombre(),
						d.getAsignatura().getCodigo()));
			}
			return new AlumnoFichaResumen(
					ac.getAlumnoUsuarioId(),
					ac.getNombre(),
					ac.getApellido(),
					ac.getCurso().getId(),
					ac.getCurso().getNombre(),
					ac.getCurso().getCodigo(),
					new ArrayList<>(asigMap.values()));
		});
	}

	private void agregarContacto(LinkedHashMap<Long, ContactoMensajeria> map, Long id, String nombre, String apellido,
			String email, String tipo, String contexto) {
		if (id == null || map.containsKey(id)) {
			return;
		}
		map.put(id, new ContactoMensajeria(id, nombre, apellido, email != null ? email : "", tipo, contexto));
	}

	private void agregarContactoAuth(LinkedHashMap<Long, ContactoMensajeria> map, Long usuarioId, String contexto,
			String tipo) {
		if (usuarioId == null || map.containsKey(usuarioId)) {
			return;
		}
		try {
			Map<String, Object> c = authContactoClient.obtenerContactoUsuario(usuarioId);
			if (c == null) {
				return;
			}
			String nombre = String.valueOf(c.getOrDefault("nombre", ""));
			String apellido = String.valueOf(c.getOrDefault("apellido", ""));
			String email = String.valueOf(c.getOrDefault("email", ""));
			map.put(usuarioId, new ContactoMensajeria(usuarioId, nombre, apellido, email, tipo, contexto));
		} catch (RuntimeException ignored) {
			// omitir si auth no responde
		}
	}

	@Transactional
	public void registrarAuditoriaContactoApoderado(Long profesorUsuarioId, Long alumnoUsuarioId) {
		AuditoriaContactoApoderado a = new AuditoriaContactoApoderado();
		a.setProfesorUsuarioId(profesorUsuarioId);
		a.setAlumnoUsuarioId(alumnoUsuarioId);
		a.setCreadoEn(Instant.now());
		auditoriaContactoApoderadoRepository.save(a);
	}

	private static boolean esAdministrador(MsUserPrincipal viewer) {
		return Roles.isAdmin(viewer.tipo());
	}

	private static boolean esAlumno(MsUserPrincipal viewer) {
		return Roles.isAlumno(viewer.tipo());
	}

	private static boolean esApoderado(MsUserPrincipal viewer) {
		return Roles.isApoderado(viewer.tipo());
	}

	public record EstadoCuenta(boolean completa, String detalle) {
	}

	public EstadoCuenta evaluarEstadoCuenta(Long usuarioId, String tipo) {
		if (tipo == null || tipo.isBlank()) {
			return new EstadoCuenta(false, "La cuenta no tiene rol asignado");
		}
		String t = tipo.trim().toLowerCase();
		if (Roles.isAdmin(t)) {
			return new EstadoCuenta(true, "Cuenta administrador");
		}
		if (Roles.isAlumno(t)) {
			if (alumnoCursoRepository.findByAlumnoUsuarioId(usuarioId).isEmpty()) {
				return new EstadoCuenta(false, "El alumno debe estar inscrito en un curso");
			}
			if (apoderadoAlumnoRepository.countByAlumnoUsuarioId(usuarioId) < 1) {
				return new EstadoCuenta(false, "El alumno debe tener al menos un apoderado vinculado");
			}
			return new EstadoCuenta(true, "Cuenta alumno completa");
		}
		if (Roles.isApoderado(t)) {
			if (apoderadoAlumnoRepository.countByApoderadoUsuarioId(usuarioId) < 1) {
				return new EstadoCuenta(false, "El apoderado debe tener al menos un pupilo asignado");
			}
			return new EstadoCuenta(true, "Cuenta apoderado completa");
		}
		if (Roles.isProfesor(t)) {
			if (!docenteAsignacionRepository.existsByProfesorUsuarioId(usuarioId)) {
				return new EstadoCuenta(false, "El profesor debe tener al menos una asignatura y curso asignados");
			}
			return new EstadoCuenta(true, "Cuenta profesor completa");
		}
		return new EstadoCuenta(false, "Rol no reconocido");
	}

	@Transactional
	public void actualizarDatosPupilo(Long apoderadoUsuarioId, Long alumnoUsuarioId, String nombre, String apellido) {
		if (!apoderadoAlumnoRepository.existsByApoderadoUsuarioIdAndAlumnoUsuarioId(apoderadoUsuarioId, alumnoUsuarioId)) {
			throw new IllegalArgumentException("No está autorizado a modificar datos de este alumno");
		}
		AlumnoCurso ac = alumnoCursoRepository.findByAlumnoUsuarioId(alumnoUsuarioId)
				.orElseThrow(() -> new IllegalArgumentException("El alumno no tiene curso asignado"));
		if (nombre != null && !nombre.isBlank()) {
			ac.setNombre(nombre.trim());
		}
		if (apellido != null && !apellido.isBlank()) {
			ac.setApellido(apellido.trim());
		}
		alumnoCursoRepository.save(ac);
		authUsuarioClient.sincronizarNombre(alumnoUsuarioId, ac.getNombre(), ac.getApellido());
	}

	public Optional<AlumnoFichaResumen> fichaPupiloApoderado(Long apoderadoUsuarioId, Long alumnoUsuarioId) {
		if (!apoderadoAlumnoRepository.existsByApoderadoUsuarioIdAndAlumnoUsuarioId(apoderadoUsuarioId, alumnoUsuarioId)) {
			return Optional.empty();
		}
		return fichaAlumno(alumnoUsuarioId);
	}

	public boolean apoderadoTienePupilo(Long apoderadoUsuarioId, Long alumnoUsuarioId) {
		return apoderadoAlumnoRepository.existsByApoderadoUsuarioIdAndAlumnoUsuarioId(apoderadoUsuarioId, alumnoUsuarioId);
	}

	public boolean puedeEnviarMensaje(Long remitenteId, String remitenteTipo, Long destinatarioId, String destinatarioTipo) {
		if (remitenteId.equals(destinatarioId)) {
			return false;
		}
		if (Roles.isAdmin(remitenteTipo) || Roles.isAdmin(destinatarioTipo)) {
			return Roles.isAdmin(remitenteTipo);
		}
		if (Roles.isAlumno(remitenteTipo) && Roles.isAlumno(destinatarioTipo)) {
			return mismosCurso(remitenteId, destinatarioId);
		}
		if (Roles.isAlumno(remitenteTipo) && Roles.isProfesor(destinatarioTipo)) {
			return profesorAtiendeAlumno(destinatarioId, remitenteId);
		}
		if (Roles.isApoderado(remitenteTipo) && Roles.isProfesor(destinatarioTipo)) {
			return apoderadoPuedeContactarProfesor(remitenteId, destinatarioId);
		}
		if (Roles.isProfesor(remitenteTipo) && Roles.isAlumno(destinatarioTipo)) {
			return profesorTieneAlumnoEnSuCurso(remitenteId, destinatarioId);
		}
		if (Roles.isProfesor(remitenteTipo) && Roles.isApoderado(destinatarioTipo)) {
			return profesorPuedeContactarApoderado(remitenteId, destinatarioId);
		}
		return false;
	}

	private boolean mismosCurso(Long alumnoA, Long alumnoB) {
		Optional<Long> cursoA = cursoIdDelAlumno(alumnoA);
		Optional<Long> cursoB = cursoIdDelAlumno(alumnoB);
		return cursoA.isPresent() && cursoA.equals(cursoB);
	}

	private boolean profesorAtiendeAlumno(Long profesorId, Long alumnoId) {
		Optional<Long> cursoId = cursoIdDelAlumno(alumnoId);
		if (cursoId.isEmpty()) {
			return false;
		}
		return docenteAsignacionRepository.existsByProfesorUsuarioIdAndCurso_Id(profesorId, cursoId.get());
	}

	private boolean apoderadoPuedeContactarProfesor(Long apoderadoId, Long profesorId) {
		for (ApoderadoAlumno v : apoderadoAlumnoRepository.findByApoderadoUsuarioId(apoderadoId)) {
			if (profesorAtiendeAlumno(profesorId, v.getAlumnoUsuarioId())) {
				return true;
			}
		}
		return false;
	}

	private boolean profesorPuedeContactarApoderado(Long profesorId, Long apoderadoId) {
		for (ApoderadoAlumno v : apoderadoAlumnoRepository.findByApoderadoUsuarioId(apoderadoId)) {
			if (profesorTieneAlumnoEnSuCurso(profesorId, v.getAlumnoUsuarioId())) {
				return true;
			}
		}
		return false;
	}
}
