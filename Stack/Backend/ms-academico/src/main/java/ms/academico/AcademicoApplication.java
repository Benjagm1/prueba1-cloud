package ms.academico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"ms.academico", "ms.common"})
public class AcademicoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AcademicoApplication.class, args);
	}
}
