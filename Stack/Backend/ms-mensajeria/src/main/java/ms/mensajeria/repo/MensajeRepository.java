package ms.mensajeria.repo;

import ms.mensajeria.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

	List<Mensaje> findByDestinatarioUsuarioIdOrderByCreadoEnDesc(Long destinatarioUsuarioId);

	List<Mensaje> findByRemitenteUsuarioIdOrderByCreadoEnDesc(Long remitenteUsuarioId);
}
