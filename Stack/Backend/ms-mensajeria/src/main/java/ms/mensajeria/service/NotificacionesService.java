package ms.mensajeria.service;

import ms.mensajeria.model.Notificacion;
import ms.mensajeria.repo.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacionesService {

	private final NotificacionRepository notificacionRepository;

	public NotificacionesService(NotificacionRepository notificacionRepository) {
		this.notificacionRepository = notificacionRepository;
	}

	@Transactional
	public Notificacion crearInterno(Long destinatarioUsuarioId, String titulo, String cuerpo) {
		Notificacion n = new Notificacion();
		n.setDestinatarioUsuarioId(destinatarioUsuarioId);
		n.setTitulo(titulo);
		n.setCuerpo(cuerpo);
		n.setLeida(false);
		return notificacionRepository.save(n);
	}

	public List<Notificacion> bandeja(Long usuarioId) {
		return notificacionRepository.findByDestinatarioUsuarioIdOrderByCreadoEnDesc(usuarioId);
	}

	@Transactional
	public void marcarLeida(Long usuarioId, Long notificacionId) {
		Notificacion n = notificacionRepository.findById(notificacionId)
				.orElseThrow(() -> new IllegalArgumentException("Notificación no existe"));
		if (!n.getDestinatarioUsuarioId().equals(usuarioId)) {
			throw new IllegalStateException("No autorizado");
		}
		n.setLeida(true);
	}
}
