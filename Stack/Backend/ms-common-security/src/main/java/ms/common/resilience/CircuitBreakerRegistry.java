package ms.common.resilience;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class CircuitBreakerRegistry {

	private static final int DEFAULT_FAILURE_THRESHOLD = 5;
	private static final Duration DEFAULT_OPEN_DURATION = Duration.ofSeconds(30);

	private final ConcurrentHashMap<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();

	public CircuitBreaker forService(String serviceName) {
		return breakers.computeIfAbsent(serviceName,
				name -> new CircuitBreaker(name, DEFAULT_FAILURE_THRESHOLD, DEFAULT_OPEN_DURATION));
	}
}
