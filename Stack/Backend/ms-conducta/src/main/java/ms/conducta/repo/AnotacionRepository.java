package ms.conducta.repo;

import ms.conducta.model.Anotacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnotacionRepository extends JpaRepository<Anotacion, Long> {

	List<Anotacion> findByAlumnoUsuarioIdOrderByCreadoEnDesc(Long alumnoUsuarioId);
}
