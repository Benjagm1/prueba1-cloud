package ms.administracion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "docente_asignacion", uniqueConstraints = @UniqueConstraint(columnNames = {"profesor_usuario_id", "asignatura_id", "curso_id"}))
public class DocenteAsignacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "profesor_usuario_id", nullable = false)
	private Long profesorUsuarioId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "asignatura_id")
	private Asignatura asignatura;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "curso_id")
	private Curso curso;

	public Long getId() {
		return id;
	}

	public Long getProfesorUsuarioId() {
		return profesorUsuarioId;
	}

	public void setProfesorUsuarioId(Long profesorUsuarioId) {
		this.profesorUsuarioId = profesorUsuarioId;
	}

	public Asignatura getAsignatura() {
		return asignatura;
	}

	public void setAsignatura(Asignatura asignatura) {
		this.asignatura = asignatura;
	}

	public Curso getCurso() {
		return curso;
	}

	public void setCurso(Curso curso) {
		this.curso = curso;
	}
}
