package ms.mensajeria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"ms.mensajeria", "ms.common"})
public class MensajeriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MensajeriaApplication.class, args);
	}
}
