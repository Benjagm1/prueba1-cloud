package ms.asistencia.repo;

import ms.asistencia.model.RegistroAsistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroAsistenciaRepository extends JpaRepository<RegistroAsistencia, Long> {

	List<RegistroAsistencia> findByAlumnoUsuarioIdAndFechaBetweenOrderByFechaAsc(Long alumnoUsuarioId, LocalDate desde,
			LocalDate hasta);

	Optional<RegistroAsistencia> findByFechaAndAlumnoUsuarioIdAndAsignaturaId(LocalDate fecha, Long alumnoUsuarioId,
			Long asignaturaId);

	long countByAlumnoUsuarioIdAndFechaBetween(Long alumnoUsuarioId, LocalDate desde, LocalDate hasta);

	long countByAlumnoUsuarioIdAndPresenteIsTrueAndFechaBetween(Long alumnoUsuarioId, LocalDate desde, LocalDate hasta);

	List<RegistroAsistencia> findByFechaAndCursoIdAndAsignaturaIdOrderByAlumnoUsuarioIdAsc(LocalDate fecha, Long cursoId,
			Long asignaturaId);

	long countByProfesorUsuarioIdAndFechaBetween(Long profesorUsuarioId, LocalDate desde, LocalDate hasta);

	long countByProfesorUsuarioIdAndPresenteIsTrueAndFechaBetween(Long profesorUsuarioId, LocalDate desde,
			LocalDate hasta);
}
