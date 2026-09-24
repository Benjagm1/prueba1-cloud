package ms.administracion.repo;

import ms.administracion.model.DocenteAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocenteAsignacionRepository extends JpaRepository<DocenteAsignacion, Long> {

	boolean existsByProfesorUsuarioIdAndAsignatura_IdAndCurso_Id(Long profesorUsuarioId, Long asignaturaId, Long cursoId);

	List<DocenteAsignacion> findByProfesorUsuarioIdAndAsignatura_Id(Long profesorUsuarioId, Long asignaturaId);

	boolean existsByProfesorUsuarioIdAndCurso_Id(Long profesorUsuarioId, Long cursoId);

	boolean existsByProfesorUsuarioId(Long profesorUsuarioId);

	List<DocenteAsignacion> findByProfesorUsuarioId(Long profesorUsuarioId);

	List<DocenteAsignacion> findByCurso_Id(Long cursoId);
}
