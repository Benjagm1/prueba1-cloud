package ms.administracion.bootstrap;

import ms.administracion.model.Asignatura;
import ms.administracion.model.Curso;
import ms.administracion.repo.AsignaturaRepository;
import ms.administracion.repo.CursoRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Catálogo inicial: 1ro básico → 4to medio y las 4 asignaturas del colegio.
 * Debe ejecutarse antes que {@link DemoColegioDataInitializer} (@Order 1).
 */
@Component
@Order(1)
public class CatalogoColegioInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(CatalogoColegioInitializer.class);

	private final CursoRepository cursoRepository;
	private final AsignaturaRepository asignaturaRepository;

	public CatalogoColegioInitializer(CursoRepository cursoRepository, AsignaturaRepository asignaturaRepository) {
		this.cursoRepository = cursoRepository;
		this.asignaturaRepository = asignaturaRepository;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (cursoRepository.count() == 0) {
			String[] nombresBasicos = {
					"1ro Básico", "2do Básico", "3ro Básico", "4to Básico",
					"5to Básico", "6to Básico", "7mo Básico", "8vo Básico"
			};
			for (int i = 0; i < nombresBasicos.length; i++) {
				Curso c = new Curso();
				c.setCodigo("BAS" + (i + 1));
				c.setNombre(nombresBasicos[i]);
				c.setOrden(i + 1);
				cursoRepository.save(c);
			}
			String[] nombresMedios = {"1ro Medio", "2do Medio", "3ro Medio", "4to Medio"};
			for (int i = 0; i < nombresMedios.length; i++) {
				Curso c = new Curso();
				c.setCodigo("MED" + (i + 1));
				c.setNombre(nombresMedios[i]);
				c.setOrden(8 + i + 1);
				cursoRepository.save(c);
			}
		}
		if (asignaturaRepository.count() == 0) {
			crearAsignatura("LNG", "Lenguaje y Comunicación");
			crearAsignatura("MAT", "Matemática");
			crearAsignatura("CSN", "Ciencias Naturales");
			crearAsignatura("EF", "Educación Física");
		}
		log.info("Catálogo listo: {} cursos, {} asignaturas", cursoRepository.count(), asignaturaRepository.count());
	}

	private void crearAsignatura(String codigo, String nombre) {
		Asignatura a = new Asignatura();
		a.setCodigo(codigo);
		a.setNombre(nombre);
		asignaturaRepository.save(a);
	}
}
