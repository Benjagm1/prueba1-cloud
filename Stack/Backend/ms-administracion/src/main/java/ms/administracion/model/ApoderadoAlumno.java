package ms.administracion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "apoderado_alumno", uniqueConstraints = @UniqueConstraint(columnNames = {"apoderado_usuario_id", "alumno_usuario_id"}))
public class ApoderadoAlumno {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "apoderado_usuario_id", nullable = false)
	private Long apoderadoUsuarioId;

	@Column(name = "alumno_usuario_id", nullable = false)
	private Long alumnoUsuarioId;

	public Long getId() {
		return id;
	}

	public Long getApoderadoUsuarioId() {
		return apoderadoUsuarioId;
	}

	public void setApoderadoUsuarioId(Long apoderadoUsuarioId) {
		this.apoderadoUsuarioId = apoderadoUsuarioId;
	}

	public Long getAlumnoUsuarioId() {
		return alumnoUsuarioId;
	}

	public void setAlumnoUsuarioId(Long alumnoUsuarioId) {
		this.alumnoUsuarioId = alumnoUsuarioId;
	}
}
