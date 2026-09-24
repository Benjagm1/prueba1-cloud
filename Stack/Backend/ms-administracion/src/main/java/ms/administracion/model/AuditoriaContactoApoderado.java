package ms.administracion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "auditoria_contacto_apoderado")
public class AuditoriaContactoApoderado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "profesor_usuario_id", nullable = false)
	private Long profesorUsuarioId;

	@Column(name = "alumno_usuario_id", nullable = false)
	private Long alumnoUsuarioId;

	@Column(nullable = false)
	private Instant creadoEn;

	public Long getId() {
		return id;
	}

	public Long getProfesorUsuarioId() {
		return profesorUsuarioId;
	}

	public void setProfesorUsuarioId(Long profesorUsuarioId) {
		this.profesorUsuarioId = profesorUsuarioId;
	}

	public Long getAlumnoUsuarioId() {
		return alumnoUsuarioId;
	}

	public void setAlumnoUsuarioId(Long alumnoUsuarioId) {
		this.alumnoUsuarioId = alumnoUsuarioId;
	}

	public Instant getCreadoEn() {
		return creadoEn;
	}

	public void setCreadoEn(Instant creadoEn) {
		this.creadoEn = creadoEn;
	}
}
