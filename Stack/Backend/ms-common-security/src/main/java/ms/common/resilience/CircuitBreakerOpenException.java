package ms.common.resilience;

public class CircuitBreakerOpenException extends RuntimeException {

	public CircuitBreakerOpenException(String message) {
		super(message);
	}
}
