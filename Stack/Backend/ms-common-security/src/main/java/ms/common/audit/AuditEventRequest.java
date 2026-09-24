package ms.common.audit;

/**
 * Evento de auditoría para trazabilidad (Ley 21.719 — datos estudiantiles).
 */
public record AuditEventRequest(
		String modulo,
		String accion,
		Long actorUsuarioId,
		String actorTipo,
		Long recursoId,
		String detalle) {
}
