package ms.mensajeria.service;

import ms.common.audit.AdministracionAuditClient;
import ms.common.security.MsUserPrincipal;
import ms.mensajeria.client.AdministracionMensajesClient;
import ms.mensajeria.client.AuthContactoClient;
import ms.mensajeria.client.AuthTipoClient;
import ms.mensajeria.model.Mensaje;
import ms.mensajeria.repo.MensajeRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class MensajesDirectosService {

	private final MensajeRepository mensajeRepository;
	private final AdministracionMensajesClient permisosClient;
	private final AuthTipoClient authTipoClient;
	private final AuthContactoClient authContactoClient;
	private final NotificacionesService notificacionesService;
	private final ObjectProvider<AdministracionAuditClient> auditClient;

	public MensajesDirectosService(
			MensajeRepository mensajeRepository,
			AdministracionMensajesClient permisosClient,
			AuthTipoClient authTipoClient,
			AuthContactoClient authContactoClient,
			NotificacionesService notificacionesService,
			ObjectProvider<AdministracionAuditClient> auditClient) {
		this.mensajeRepository = mensajeRepository;
		this.permisosClient = permisosClient;
		this.authTipoClient = authTipoClient;
		this.authContactoClient = authContactoClient;
		this.notificacionesService = notificacionesService;
		this.auditClient = auditClient;
	}

	public record MensajeBandejaDto(
			Long id,
			String asunto,
			String cuerpo,
			boolean leido,
			Instant creadoEn,
			Long remitenteUsuarioId,
			String remitenteNombre,
			String remitenteApellido) {
	}

	@Transactional
	public Mensaje enviar(MsUserPrincipal remitente, Long destinatarioId, String asunto, String cuerpo) {
		String destinatarioTipo = authTipoClient.tipoUsuario(destinatarioId);
		if (!permisosClient.puedeEnviar(remitente.userId(), remitente.tipo(), destinatarioId, destinatarioTipo)) {
			throw new IllegalStateException("No tiene permiso para enviar mensaje a este usuario");
		}
		Mensaje m = new Mensaje();
		m.setRemitenteUsuarioId(remitente.userId());
		m.setDestinatarioUsuarioId(destinatarioId);
		m.setAsunto(asunto != null && !asunto.isBlank() ? asunto.trim() : "Mensaje");
		m.setCuerpo(cuerpo.trim());
		m.setLeido(false);
		Mensaje guardado = mensajeRepository.save(m);
		notificacionesService.crearInterno(destinatarioId, "Nuevo mensaje: " + guardado.getAsunto(), guardado.getCuerpo());
		auditClient.ifAvailable(client -> client.registrarSilencioso(
				"mensajeria",
				"MENSAJE_ENVIADO",
				remitente.userId(),
				remitente.tipo(),
				destinatarioId,
				"Asunto: " + guardado.getAsunto()));
		return guardado;
	}

	public List<MensajeBandejaDto> recibidos(Long usuarioId) {
		return mensajeRepository.findByDestinatarioUsuarioIdOrderByCreadoEnDesc(usuarioId).stream()
				.map(this::aBandejaDto)
				.toList();
	}

	private MensajeBandejaDto aBandejaDto(Mensaje m) {
		String nombre = "";
		String apellido = "";
		try {
			Map<String, Object> contacto = authContactoClient.obtenerContactoUsuario(m.getRemitenteUsuarioId());
			if (contacto != null) {
				nombre = contacto.get("nombre") != null ? contacto.get("nombre").toString() : "";
				apellido = contacto.get("apellido") != null ? contacto.get("apellido").toString() : "";
			}
		} catch (RuntimeException ignored) {
			// contacto opcional
		}
		return new MensajeBandejaDto(
				m.getId(),
				m.getAsunto(),
				m.getCuerpo(),
				m.isLeido(),
				m.getCreadoEn(),
				m.getRemitenteUsuarioId(),
				nombre,
				apellido);
	}

	public List<Mensaje> enviados(Long usuarioId) {
		return mensajeRepository.findByRemitenteUsuarioIdOrderByCreadoEnDesc(usuarioId);
	}

	@Transactional
	public void marcarMensajeLeido(Long usuarioId, Long mensajeId) {
		Mensaje m = mensajeRepository.findById(mensajeId)
				.orElseThrow(() -> new IllegalArgumentException("Mensaje no existe"));
		if (!m.getDestinatarioUsuarioId().equals(usuarioId)) {
			throw new IllegalStateException("No autorizado");
		}
		m.setLeido(true);
	}
}
