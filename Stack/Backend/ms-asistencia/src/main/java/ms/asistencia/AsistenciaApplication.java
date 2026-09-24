package ms.asistencia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"ms.asistencia", "ms.common"})
public class AsistenciaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsistenciaApplication.class, args);
	}
}
