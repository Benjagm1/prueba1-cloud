package ms.administracion.bootstrap;



import ms.administracion.client.AuthUsuarioClient;

import ms.administracion.model.Asignatura;

import ms.administracion.model.AlumnoCurso;

import ms.administracion.model.Curso;

import ms.administracion.repo.AlumnoCursoRepository;

import ms.administracion.repo.AsignaturaRepository;

import ms.administracion.repo.CursoRepository;

import ms.administracion.repo.DocenteAsignacionRepository;

import ms.administracion.service.AdministracionDominioService;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.ApplicationArguments;

import org.springframework.boot.ApplicationRunner;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.core.annotation.Order;

import org.springframework.stereotype.Component;



import java.util.List;

import java.util.Map;

import java.util.Optional;



/**

 * Datos demo: profesor {@code profesor@boh.cl}, 4 cursos de media (1°–4°) con 12 alumnos distintos

 * cada uno y un apoderado vinculado por alumno (cuentas en auth + inscripción + vínculo familiar).

 */

@Component

@Order(100)

@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true", matchIfMissing = true)

public class DemoColegioDataInitializer implements ApplicationRunner {



	private static final Logger log = LoggerFactory.getLogger(DemoColegioDataInitializer.class);

	private static final int CATALOG_WAIT_ATTEMPTS = 15;

	private static final long CATALOG_WAIT_MS = 500;

	private static final int AUTH_PROFESOR_ATTEMPTS = 12;

	private static final long AUTH_PROFESOR_WAIT_MS = 2_000;

	private static final List<String> CODIGOS_MEDIA = List.of("MED1", "MED2", "MED3", "MED4");



	/** 48 familias únicas: [nombreAlumno, apellidoAlumno, nombreApoderado, apellidoApoderado] */

	private static final String[][] PLANTILLA_FAMILIAS = {

			// 1° Medio

			{ "Martina", "Rojas", "Carlos", "Rojas" },

			{ "Tomás", "Silva", "Patricia", "Silva" },

			{ "Valentina", "Muñoz", "Jorge", "Muñoz" },

			{ "Benjamín", "Fuentes", "Andrea", "Fuentes" },

			{ "Isidora", "Contreras", "Roberto", "Contreras" },

			{ "Agustín", "Torres", "Claudia", "Torres" },

			{ "Emilia", "Vargas", "Felipe", "Vargas" },

			{ "Maximiliano", "Pizarro", "Mónica", "Pizarro" },

			{ "Sofía", "Herrera", "Diego", "Herrera" },

			{ "Matías", "Castillo", "Verónica", "Castillo" },

			{ "Javiera", "Morales", "Ricardo", "Morales" },

			{ "Vicente", "Sepúlveda", "Carolina", "Sepúlveda" },

			// 2° Medio

			{ "Camila", "Araya", "Hernán", "Araya" },

			{ "Nicolás", "Bravo", "Lorena", "Bravo" },

			{ "Florencia", "Carmona", "Mauricio", "Carmona" },

			{ "Cristóbal", "Díaz", "Silvia", "Díaz" },

			{ "Antonia", "Espinoza", "Pablo", "Espinoza" },

			{ "Franco", "Gutiérrez", "Marcela", "Gutiérrez" },

			{ "Catalina", "Ibáñez", "Eduardo", "Ibáñez" },

			{ "Sebastián", "Jara", "Ximena", "Jara" },

			{ "Constanza", "López", "Álvaro", "López" },

			{ "Diego", "Maldonado", "Daniela", "Maldonado" },

			{ "Francisca", "Navarro", "Gonzalo", "Navarro" },

			{ "Gabriel", "Ortiz", "Paulina", "Ortiz" },

			// 3° Medio

			{ "Amanda", "Paredes", "Sergio", "Paredes" },

			{ "Ignacio", "Quezada", "Teresa", "Quezada" },

			{ "Daniela", "Ramírez", "Iván", "Ramírez" },

			{ "Luciano", "Salazar", "Natalia", "Salazar" },

			{ "Fernanda", "Tapia", "Oscar", "Tapia" },

			{ "Javier", "Uribe", "Camilo", "Uribe" },

			{ "Paula", "Venegas", "Esteban", "Venegas" },

			{ "Rodrigo", "Wladimir", "Jimena", "Wladimir" },

			{ "Bárbara", "Yáñez", "César", "Yáñez" },

			{ "Andrés", "Zamorano", "Valeria", "Zamorano" },

			{ "Carola", "Abarca", "Felipe", "Abarca" },

			{ "Felipe", "Baeza", "Isabel", "Baeza" },

			// 4° Medio

			{ "Josefa", "Cáceres", "Manuel", "Cáceres" },

			{ "Leonardo", "Donoso", "Rosa", "Donoso" },

			{ "Maite", "Escobar", "Arturo", "Escobar" },

			{ "Pablo", "Figueroa", "Gloria", "Figueroa" },

			{ "Renata", "Gaete", "Hugo", "Gaete" },

			{ "Óscar", "Huenchuleo", "Luz", "Huenchuleo" },

			{ "Trinidad", "Inostroza", "Marco", "Inostroza" },

			{ "Matías", "Jorquera", "Olga", "Jorquera" },

			{ "Antonia", "Keim", "Raúl", "Keim" },

			{ "Renato", "Larraín", "Susana", "Larraín" },

			{ "Sofía", "Molina", "Víctor", "Molina" },

			{ "Tomás", "Núñez", "Yolanda", "Núñez" },

	};



	private static final String CLAVE_ALUMNO = "Alumno123!";

	private static final String CLAVE_APODERADO = "Apoderado123!";



	private final DocenteAsignacionRepository docenteAsignacionRepository;

	private final AlumnoCursoRepository alumnoCursoRepository;

	private final CursoRepository cursoRepository;

	private final AsignaturaRepository asignaturaRepository;

	private final AuthUsuarioClient authUsuarioClient;

	private final AdministracionDominioService dominio;

	private final String demoProfesorEmail;

	private final String demoProfesorPassword;

	private final int alumnosPorCurso;



	public DemoColegioDataInitializer(

			DocenteAsignacionRepository docenteAsignacionRepository,

			AlumnoCursoRepository alumnoCursoRepository,

			CursoRepository cursoRepository,

			AsignaturaRepository asignaturaRepository,

			AuthUsuarioClient authUsuarioClient,

			AdministracionDominioService dominio,

			@Value("${app.demo.profesor.email:profesor@boh.cl}") String demoProfesorEmail,

			@Value("${app.demo.profesor.password:MiClave123!}") String demoProfesorPassword,

			@Value("${app.demo.alumnos-por-curso:12}") int alumnosPorCurso) {

		this.docenteAsignacionRepository = docenteAsignacionRepository;

		this.alumnoCursoRepository = alumnoCursoRepository;

		this.cursoRepository = cursoRepository;

		this.asignaturaRepository = asignaturaRepository;

		this.authUsuarioClient = authUsuarioClient;

		this.dominio = dominio;

		this.demoProfesorEmail = demoProfesorEmail.trim().toLowerCase();

		this.demoProfesorPassword = demoProfesorPassword;

		this.alumnosPorCurso = alumnosPorCurso;

	}



	@Override

	public void run(ApplicationArguments args) {

		if (!esperarCatalogoListo()) {

			log.warn("Demo: catálogo incompleto tras {} intentos; no se cargan alumnos demo", CATALOG_WAIT_ATTEMPTS);

			return;

		}

		Optional<Asignatura> matOpt = asignaturaRepository.findAll().stream()

				.filter(a -> "MAT".equalsIgnoreCase(a.getCodigo()))

				.findFirst();

		if (matOpt.isEmpty()) {

			log.warn("Demo: asignatura MAT no encontrada");

			return;

		}

		Long asignaturaId = matOpt.get().getId();

		List<Curso> cursosMedio = CODIGOS_MEDIA.stream()

				.map(cursoRepository::findByCodigo)

				.flatMap(Optional::stream)

				.toList();

		if (cursosMedio.isEmpty()) {

			log.warn("Demo: no hay cursos de enseñanza media (MED1–MED4)");

			return;

		}

		Long profesorId = resolverProfesorDemo();

		if (profesorId == null) {

			log.warn("Demo: no se pudo resolver profesor {} tras reintentos", demoProfesorEmail);

			return;

		}

		for (Curso curso : cursosMedio) {

			if (!docenteAsignacionRepository.existsByProfesorUsuarioIdAndAsignatura_IdAndCurso_Id(

					profesorId, asignaturaId, curso.getId())) {

				dominio.asignarDocenteAsignaturaCurso(profesorId, asignaturaId, curso.getId());

			}

			asegurarAlumnosYApoderados(curso);

		}

		log.info("Demo: {} cursos de media con {} alumnos únicos y apoderado c/u", cursosMedio.size(), alumnosPorCurso);

	}

	private boolean esperarCatalogoListo() {

		for (int intento = 0; intento < CATALOG_WAIT_ATTEMPTS; intento++) {

			boolean tieneMat = asignaturaRepository.findAll().stream()

					.anyMatch(a -> "MAT".equalsIgnoreCase(a.getCodigo()));

			boolean tieneMedios = CODIGOS_MEDIA.stream()

					.allMatch(cod -> cursoRepository.findByCodigo(cod).isPresent());

			if (tieneMat && tieneMedios) {

				return true;

			}

			if (intento + 1 < CATALOG_WAIT_ATTEMPTS) {

				log.debug("Demo: esperando catálogo (intento {}/{})", intento + 1, CATALOG_WAIT_ATTEMPTS);

				dormir(CATALOG_WAIT_MS);

			}

		}

		return false;

	}

	private static void dormir(long millis) {

		try {

			Thread.sleep(millis);

		} catch (InterruptedException ex) {

			Thread.currentThread().interrupt();

		}

	}



	private void asegurarAlumnosYApoderados(Curso curso) {

		int cursoIdx = CODIGOS_MEDIA.indexOf(curso.getCodigo());

		if (cursoIdx < 0) {

			return;

		}

		String cod = curso.getCodigo().toLowerCase();

		List<AlumnoCurso> inscritos = alumnoCursoRepository.findByCurso_IdOrderByApellidoAscNombreAsc(curso.getId());



		for (int i = 0; i < alumnosPorCurso; i++) {

			String[] familia = familiaPara(cursoIdx, i);

			int numero = i + 1;

			String alumnoEmail = String.format("alumno.%s.%02d@estudiantes.boh.cl", cod, numero);

			String apoderadoEmail = String.format("apoderado.%s.%02d@apoderados.boh.cl", cod, numero);



			Long alumnoId = resolverAlumnoDemo(curso, inscritos, familia, alumnoEmail);

			if (alumnoId == null) {

				continue;

			}



			sincronizarAlumnoCursoPorId(curso.getId(), alumnoId, familia[0], familia[1]);

			authUsuarioClient.sincronizarDemo(alumnoId, familia[0], familia[1], CLAVE_ALUMNO, alumnoEmail);

			vincularApoderado(alumnoId, familia, apoderadoEmail);

		}

	}



	private Long resolverAlumnoDemo(Curso curso, List<AlumnoCurso> inscritos, String[] familia, String alumnoEmail) {

		Long porEmailCanonico = authUsuarioClient.buscarIdPorEmail(alumnoEmail);

		if (porEmailCanonico != null) {

			asegurarInscripcion(curso, porEmailCanonico, familia[0], familia[1]);

			return porEmailCanonico;

		}



		for (AlumnoCurso ac : inscritos) {

			if (nombresIguales(ac.getNombre(), ac.getApellido(), familia[0], familia[1])) {

				return ac.getAlumnoUsuarioId();

			}

		}



		Long creado = crearSiFalta(familia[0], familia[1], alumnoEmail, CLAVE_ALUMNO, "alumno");

		if (creado != null) {

			asegurarInscripcion(curso, creado, familia[0], familia[1]);

		}

		return creado;

	}



	private void asegurarInscripcion(Curso curso, Long alumnoId, String nombre, String apellido) {

		if (alumnoCursoRepository.findByAlumnoUsuarioId(alumnoId).isPresent()) {

			sincronizarAlumnoCursoPorId(curso.getId(), alumnoId, nombre, apellido);

			return;

		}

		try {

			dominio.inscribirAlumnoEnCurso(alumnoId, curso.getId(), nombre, apellido);

		} catch (RuntimeException ex) {

			log.debug("Demo: alumno {} ya inscrito en otro curso ({})", alumnoId, ex.getMessage());

		}

	}



	private void sincronizarAlumnoCursoPorId(Long cursoId, Long alumnoUsuarioId, String nombre, String apellido) {

		alumnoCursoRepository.findByAlumnoUsuarioId(alumnoUsuarioId)

				.filter(ac -> ac.getCurso().getId().equals(cursoId))

				.ifPresent(ac -> sincronizarAlumnoCurso(ac, nombre, apellido));

	}



	private static boolean nombresIguales(String nombre, String apellido, String esperadoNombre, String esperadoApellido) {

		return nombre != null && apellido != null

				&& nombre.equalsIgnoreCase(esperadoNombre.trim())

				&& apellido.equalsIgnoreCase(esperadoApellido.trim());

	}



	private static String[] familiaPara(int cursoIdx, int slot) {

		int idx = cursoIdx * 12 + slot;

		if (idx < 0 || idx >= PLANTILLA_FAMILIAS.length) {

			return PLANTILLA_FAMILIAS[slot % PLANTILLA_FAMILIAS.length];

		}

		return PLANTILLA_FAMILIAS[idx];

	}



	private void sincronizarAlumnoCurso(AlumnoCurso ac, String nombre, String apellido) {

		if (nombre.equals(ac.getNombre()) && apellido.equals(ac.getApellido())) {

			return;

		}

		ac.setNombre(nombre);

		ac.setApellido(apellido);

		alumnoCursoRepository.save(ac);

	}



	private void vincularApoderado(Long alumnoUsuarioId, String[] familia, String apoderadoEmail) {

		Long apoderadoId = crearSiFalta(familia[2], familia[3], apoderadoEmail, CLAVE_APODERADO, "apoderado");

		if (apoderadoId == null) {

			return;

		}

		authUsuarioClient.sincronizarDemo(apoderadoId, familia[2], familia[3], CLAVE_APODERADO, apoderadoEmail);

		dominio.reconciliarVinculoApoderadoAlumno(apoderadoId, alumnoUsuarioId);

	}



	private Long resolverProfesorDemo() {

		for (int intento = 0; intento < AUTH_PROFESOR_ATTEMPTS; intento++) {

			Long existente = authUsuarioClient.buscarIdPorEmail(demoProfesorEmail);

			if (existente != null) {

				return existente;

			}

			try {

				Map<String, Object> creado = authUsuarioClient.crearUsuario(

						"Ana", "Pérez", demoProfesorEmail, demoProfesorPassword, "profesor");

				if (creado != null && creado.get("id") != null) {

					return ((Number) creado.get("id")).longValue();

				}

			} catch (RuntimeException ex) {

				log.debug("Demo: intento {}/{} profesor en auth: {}", intento + 1, AUTH_PROFESOR_ATTEMPTS,

						ex.getMessage());

			}

			existente = authUsuarioClient.buscarIdPorEmail(demoProfesorEmail);

			if (existente != null) {

				return existente;

			}

			if (intento + 1 < AUTH_PROFESOR_ATTEMPTS) {

				dormir(AUTH_PROFESOR_WAIT_MS);

			}

		}

		return null;

	}



	private Long crearSiFalta(String nombre, String apellido, String email, String clave, String tipo) {

		try {

			Long id = authUsuarioClient.buscarIdPorEmail(email);

			if (id != null) {

				return id;

			}

			Map<String, Object> creado = authUsuarioClient.crearUsuario(nombre, apellido, email, clave, tipo);

			if (creado != null && creado.get("id") != null) {

				return ((Number) creado.get("id")).longValue();

			}

			return authUsuarioClient.buscarIdPorEmail(email);

		} catch (RuntimeException ex) {

			log.debug("Demo: no se pudo crear usuario {} ({})", email, ex.getMessage());

			return authUsuarioClient.buscarIdPorEmail(email);

		}

	}

}


