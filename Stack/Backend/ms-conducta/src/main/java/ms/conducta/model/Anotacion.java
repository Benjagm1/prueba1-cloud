package ms.conducta.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "anotaciones")
public class Anotacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "alumno_usuario_id", nullable = false)
	private Long alumnoUsuarioId;

	@Column(name = "profesor_usuario_id", nullable = false)
	private Long profesorUsuarioId;

	@Column(name = "curso_id")
	private Long cursoId;

	@Column(name = "asignatura_id")
	private Long asignaturaId;

	@Lob
	@Column(nullable = false)
	private String texto;

	@Column(nullable = false, length = 20)
	private String tipo = "positiva";

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

	public Long getProfesorUsuarioId() {
		return profesorUsuarioId;
	}

	public void setProfesorUsuarioId(Long profesorUsuarioId) {
		this.profesorUsuarioId = profesorUsuarioId;
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

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Instant getCreadoEn() {
		return creadoEn;
	}

	public void setCreadoEn(Instant creadoEn) {
		this.creadoEn = creadoEn;
	}
}
