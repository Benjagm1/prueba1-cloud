package ms.common.security;

/**
 * Roles del libro de clases (minúsculas, unificados en todos los microservicios).
 */
public final class Roles {

	public static final String ALUMNO = "alumno";
	public static final String APODERADO = "apoderado";
	public static final String PROFESOR = "profesor";
	public static final String ADMIN = "admin";

	private Roles() {
	}

	public static boolean isAlumno(String tipo) {
		return tipo != null && ALUMNO.equalsIgnoreCase(tipo);
	}

	public static boolean isApoderado(String tipo) {
		return tipo != null && APODERADO.equalsIgnoreCase(tipo);
	}

	public static boolean isProfesor(String tipo) {
		return tipo != null && PROFESOR.equalsIgnoreCase(tipo);
	}

	public static boolean isAdmin(String tipo) {
		return tipo != null && ADMIN.equalsIgnoreCase(tipo);
	}
}
