package ms.administracion.repo;

import ms.administracion.model.AlumnoCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlumnoCursoRepository extends JpaRepository<AlumnoCurso, Long> {

	Optional<AlumnoCurso> findByAlumnoUsuarioId(Long alumnoUsuarioId);

	List<AlumnoCurso> findByCurso_IdOrderByApellidoAscNombreAsc(Long cursoId);

	long countByCurso_Id(Long cursoId);
}
