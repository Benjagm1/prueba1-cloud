package ms.academico.repo;

import ms.academico.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

	List<Calificacion> findByAlumnoUsuarioIdOrderByAsignaturaIdAscCreadoEnAsc(Long alumnoUsuarioId);

	Optional<Calificacion> findFirstByAlumnoUsuarioIdAndCursoIdAndAsignaturaIdAndNombreEvaluacionOrderByCreadoEnDesc(
			Long alumnoUsuarioId, Long cursoId, Long asignaturaId, String nombreEvaluacion);
}
