package ms.mensajeria.repo;

import ms.mensajeria.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

	List<Notificacion> findByDestinatarioUsuarioIdOrderByCreadoEnDesc(Long destinatarioUsuarioId);
}
