package ms.mensajeria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "destinatario_usuario_id", nullable = false)
	private Long destinatarioUsuarioId;

	@Column(nullable = false, length = 200)
	private String titulo;

	@Column(nullable = false, length = 4000)
	private String cuerpo;

	@Column(nullable = false)
	private boolean leida;

	@Column(nullable = false)
	private Instant creadoEn = Instant.now();

	public Long getId() {
		return id;
	}

	public Long getDestinatarioUsuarioId() {
		return destinatarioUsuarioId;
	}

	public void setDestinatarioUsuarioId(Long destinatarioUsuarioId) {
		this.destinatarioUsuarioId = destinatarioUsuarioId;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getCuerpo() {
		return cuerpo;
	}

	public void setCuerpo(String cuerpo) {
		this.cuerpo = cuerpo;
	}

	public boolean isLeida() {
		return leida;
	}

	public void setLeida(boolean leida) {
		this.leida = leida;
	}

	public Instant getCreadoEn() {
		return creadoEn;
	}

	public void setCreadoEn(Instant creadoEn) {
		this.creadoEn = creadoEn;
	}
}
