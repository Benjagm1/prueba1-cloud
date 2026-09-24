package ms.mensajeria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "mensajes")
public class Mensaje {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "remitente_usuario_id", nullable = false)
	private Long remitenteUsuarioId;

	@Column(name = "destinatario_usuario_id", nullable = false)
	private Long destinatarioUsuarioId;

	@Column(nullable = false, length = 200)
	private String asunto;

	@Column(nullable = false, length = 4000)
	private String cuerpo;

	@Column(nullable = false)
	private boolean leido;

	@Column(nullable = false)
	private Instant creadoEn = Instant.now();

	public Long getId() {
		return id;
	}

	public Long getRemitenteUsuarioId() {
		return remitenteUsuarioId;
	}

	public void setRemitenteUsuarioId(Long remitenteUsuarioId) {
		this.remitenteUsuarioId = remitenteUsuarioId;
	}

	public Long getDestinatarioUsuarioId() {
		return destinatarioUsuarioId;
	}

	public void setDestinatarioUsuarioId(Long destinatarioUsuarioId) {
		this.destinatarioUsuarioId = destinatarioUsuarioId;
	}

	public String getAsunto() {
		return asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}

	public String getCuerpo() {
		return cuerpo;
	}

	public void setCuerpo(String cuerpo) {
		this.cuerpo = cuerpo;
	}

	public boolean isLeido() {
		return leido;
	}

	public void setLeido(boolean leido) {
		this.leido = leido;
	}

	public Instant getCreadoEn() {
		return creadoEn;
	}
}
