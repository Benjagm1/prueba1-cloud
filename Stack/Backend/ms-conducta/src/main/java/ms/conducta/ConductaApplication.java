package ms.conducta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"ms.conducta", "ms.common"})
public class ConductaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConductaApplication.class, args);
	}
}
