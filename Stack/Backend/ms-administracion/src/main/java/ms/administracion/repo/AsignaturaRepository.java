package ms.administracion.repo;

import ms.administracion.model.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AsignaturaRepository extends JpaRepository<Asignatura, Long> {

	Optional<Asignatura> findByCodigo(String codigo);
}
