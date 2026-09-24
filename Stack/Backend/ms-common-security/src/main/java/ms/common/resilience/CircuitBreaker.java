package ms.common.resilience;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Circuit breaker ligero para llamadas HTTP entre microservicios.
 * Evita fallos en cascada cuando un servicio dependiente no responde.
 */
public final class CircuitBreaker {

	public enum State {
		CLOSED, OPEN, HALF_OPEN
	}

	private final String name;
	private final int failureThreshold;
	private final Duration openDuration;
	private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
	private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
	private volatile Instant openedAt = Instant.EPOCH;

	public CircuitBreaker(String name, int failureThreshold, Duration openDuration) {
		this.name = name;
		this.failureThreshold = failureThreshold;
		this.openDuration = openDuration;
	}

	public String name() {
		return name;
	}

	public State state() {
		transitionIfExpired();
		return state.get();
	}

	public <T> T execute(Supplier<T> action) {
		transitionIfExpired();
		if (state.get() == State.OPEN) {
			throw new CircuitBreakerOpenException(
					"Circuit breaker abierto para " + name + ". El servicio dependiente no está disponible.");
		}
		try {
			T result = action.get();
			onSuccess();
			return result;
		} catch (RuntimeException ex) {
			onFailure();
			throw ex;
		}
	}

	public void executeVoid(Runnable action) {
		execute(() -> {
			action.run();
			return null;
		});
	}

	private void onSuccess() {
		consecutiveFailures.set(0);
		state.set(State.CLOSED);
	}

	private void onFailure() {
		int failures = consecutiveFailures.incrementAndGet();
		if (failures >= failureThreshold) {
			state.set(State.OPEN);
			openedAt = Instant.now();
		}
	}

	private void transitionIfExpired() {
		if (state.get() == State.OPEN && Instant.now().isAfter(openedAt.plus(openDuration))) {
			state.compareAndSet(State.OPEN, State.HALF_OPEN);
		}
	}
}
