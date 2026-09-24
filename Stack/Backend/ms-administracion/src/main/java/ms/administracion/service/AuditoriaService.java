package ms.administracion.service;

import java.time.Instant;
import java.util.List;

import ms.administracion.model.AuditoriaEvento;
import ms.administracion.repository.AuditoriaEventoRepository;
import ms.common.audit.AuditEventRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

	private final AuditoriaEventoRepository repository;

	public AuditoriaService(AuditoriaEventoRepository repository) {
		this.repository = repository;
	}

	public List<AuditoriaEvento> listarRecientes(int limite) {
		int size = Math.max(1, Math.min(limite, 500));
		return repository.findAllByOrderByCreadoEnDesc(PageRequest.of(0, size));
	}

	@Transactional
	public void registrarAdmin(Long actorId, String accion, Long recursoId, String detalle) {
		registrar(new AuditEventRequest("admin", accion, actorId, "admin", recursoId, detalle));
	}

	@Transactional
	public void registrar(AuditEventRequest evento) {
		AuditoriaEvento fila = new AuditoriaEvento();
		fila.setModulo(truncar(evento.modulo(), 40));
		fila.setAccion(truncar(evento.accion(), 80));
		fila.setActorUsuarioId(evento.actorUsuarioId());
		fila.setActorTipo(truncar(evento.actorTipo(), 20));
		fila.setRecursoId(evento.recursoId());
		fila.setDetalle(truncar(evento.detalle(), 500));
		fila.setCreadoEn(Instant.now());
		repository.save(fila);
	}

	private static String truncar(String valor, int max) {
		if (valor == null) {
			return null;
		}
		return valor.length() <= max ? valor : valor.substring(0, max);
	}
}
