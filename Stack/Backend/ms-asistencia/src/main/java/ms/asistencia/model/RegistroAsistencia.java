package ms.asistencia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

@Entity
@Table(name = "registro_asistencia", uniqueConstraints = @UniqueConstraint(columnNames = {"fecha", "alumno_usuario_id", "asignatura_id"}))
public class RegistroAsistencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDate fecha;

	@Column(name = "alumno_usuario_id", nullable = false)
	private Long alumnoUsuarioId;

	@Column(name = "curso_id", nullable = false)
	private Long cursoId;

	@Column(name = "asignatura_id", nullable = false)
	private Long asignaturaId;

	@Column(nullable = false)
	private boolean presente;

	@Column(name = "profesor_usuario_id", nullable = false)
	private Long profesorUsuarioId;

	public Long getId() {
		return id;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public Long getAlumnoUsuarioId() {
		return alumnoUsuarioId;
	}

	public void setAlumnoUsuarioId(Long alumnoUsuarioId) {
		this.alumnoUsuarioId = alumnoUsuarioId;
	}

	public Long getCursoId() {
		return cursoId;
	}

	public void setCursoId(Long cursoId) {
		this.cursoId = cursoId;
	}

	public Long getAsignaturaId() {
		return asignaturaId;
	}

	public void setAsignaturaId(Long asignaturaId) {
		this.asignaturaId = asignaturaId;
	}

	public boolean isPresente() {
		return presente;
	}

	public void setPresente(boolean presente) {
		this.presente = presente;
	}

	public Long getProfesorUsuarioId() {
		return profesorUsuarioId;
	}

	public void setProfesorUsuarioId(Long profesorUsuarioId) {
		this.profesorUsuarioId = profesorUsuarioId;
	}
}
