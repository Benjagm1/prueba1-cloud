package ms.asistencia.realtime;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Difusión simple para HU2 (apoderado): eventos cuando cambia la asistencia de un alumno.
 */
@Component
public class AsistenciaSseHub {

	private final Map<Long, CopyOnWriteArrayList<SseEmitter>> porAlumno = new ConcurrentHashMap<>();

	public SseEmitter suscribir(Long alumnoUsuarioId) {
		SseEmitter emitter = new SseEmitter(0L);
		porAlumno.computeIfAbsent(alumnoUsuarioId, k -> new CopyOnWriteArrayList<>()).add(emitter);
		emitter.onCompletion(() -> remover(alumnoUsuarioId, emitter));
		emitter.onTimeout(() -> remover(alumnoUsuarioId, emitter));
		return emitter;
	}

	public void publicarActualizacion(Long alumnoUsuarioId) {
		List<SseEmitter> lista = porAlumno.get(alumnoUsuarioId);
		if (lista == null) {
			return;
		}
		for (SseEmitter emitter : lista) {
			try {
				emitter.send(SseEmitter.event().name("asistencia").data("updated"));
			} catch (IOException e) {
				emitter.completeWithError(e);
			}
		}
	}

	private void remover(Long alumnoUsuarioId, SseEmitter emitter) {
		CopyOnWriteArrayList<SseEmitter> lista = porAlumno.get(alumnoUsuarioId);
		if (lista != null) {
			lista.remove(emitter);
		}
	}
}
