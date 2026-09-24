package ms.administracion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"ms.administracion", "ms.common"})
public class AdministracionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdministracionApplication.class, args);
	}
}
