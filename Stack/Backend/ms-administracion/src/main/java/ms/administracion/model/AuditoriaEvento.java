package ms.administracion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "auditoria_eventos")
public class AuditoriaEvento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 40)
	private String modulo;

	@Column(nullable = false, length = 80)
	private String accion;

	@Column(name = "actor_usuario_id")
	private Long actorUsuarioId;

	@Column(name = "actor_tipo", length = 20)
	private String actorTipo;

	@Column(name = "recurso_id")
	private Long recursoId;

	@Column(length = 500)
	private String detalle;

	@Column(nullable = false)
	private Instant creadoEn;

	public Long getId() {
		return id;
	}

	public String getModulo() {
		return modulo;
	}

	public void setModulo(String modulo) {
		this.modulo = modulo;
	}

	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}

	public Long getActorUsuarioId() {
		return actorUsuarioId;
	}

	public void setActorUsuarioId(Long actorUsuarioId) {
		this.actorUsuarioId = actorUsuarioId;
	}

	public String getActorTipo() {
		return actorTipo;
	}

	public void setActorTipo(String actorTipo) {
		this.actorTipo = actorTipo;
	}

	public Long getRecursoId() {
		return recursoId;
	}

	public void setRecursoId(Long recursoId) {
		this.recursoId = recursoId;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public Instant getCreadoEn() {
		return creadoEn;
	}

	public void setCreadoEn(Instant creadoEn) {
		this.creadoEn = creadoEn;
	}
}
