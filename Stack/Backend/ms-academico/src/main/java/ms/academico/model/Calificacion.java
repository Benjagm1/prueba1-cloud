package ms.academico.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "calificaciones")
public class Calificacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "alumno_usuario_id", nullable = false)
	private Long alumnoUsuarioId;

	@Column(name = "curso_id", nullable = false)
	private Long cursoId;

	@Column(name = "asignatura_id", nullable = false)
	private Long asignaturaId;

	@Column(name = "profesor_usuario_id", nullable = false)
	private Long profesorUsuarioId;

	@Column(nullable = false, length = 120)
	private String nombreEvaluacion;

	@Column(nullable = false, precision = 3, scale = 1)
	private BigDecimal nota;

	@Column(nullable = false)
	private Instant creadoEn = Instant.now();

	public Long getId() {
		return id;
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

	public Long getProfesorUsuarioId() {
		return profesorUsuarioId;
	}

	public void setProfesorUsuarioId(Long profesorUsuarioId) {
		this.profesorUsuarioId = profesorUsuarioId;
	}

	public String getNombreEvaluacion() {
		return nombreEvaluacion;
	}

	public void setNombreEvaluacion(String nombreEvaluacion) {
		this.nombreEvaluacion = nombreEvaluacion;
	}

	public BigDecimal getNota() {
		return nota;
	}

	public void setNota(BigDecimal nota) {
		this.nota = nota;
	}

	public Instant getCreadoEn() {
		return creadoEn;
	}

	public void setCreadoEn(Instant creadoEn) {
		this.creadoEn = creadoEn;
	}
}
