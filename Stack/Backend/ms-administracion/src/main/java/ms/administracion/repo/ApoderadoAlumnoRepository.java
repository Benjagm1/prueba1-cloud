package ms.administracion.repo;

import ms.administracion.model.ApoderadoAlumno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApoderadoAlumnoRepository extends JpaRepository<ApoderadoAlumno, Long> {

	List<ApoderadoAlumno> findByApoderadoUsuarioId(Long apoderadoUsuarioId);

	List<ApoderadoAlumno> findByAlumnoUsuarioId(Long alumnoUsuarioId);

	boolean existsByApoderadoUsuarioIdAndAlumnoUsuarioId(Long apoderadoUsuarioId, Long alumnoUsuarioId);

	long countByApoderadoUsuarioId(Long apoderadoUsuarioId);

	long countByAlumnoUsuarioId(Long alumnoUsuarioId);
}
