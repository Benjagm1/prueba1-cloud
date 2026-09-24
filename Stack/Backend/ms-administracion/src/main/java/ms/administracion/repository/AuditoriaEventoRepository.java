package ms.administracion.repository;

import java.util.List;

import ms.administracion.model.AuditoriaEvento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento, Long> {

	List<AuditoriaEvento> findAllByOrderByCreadoEnDesc(Pageable pageable);
}
