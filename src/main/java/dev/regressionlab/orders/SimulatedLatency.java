package dev.regressionlab.orders;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simulates the network + execution time of a database round-trip.
 * Adds +/-15% jitter so latency distributions look realistic.
 */
@Component
public class SimulatedLatency {

    private final boolean enabled;

    public SimulatedLatency(@Value("${lab.simulated-latency.enabled:true}") boolean enabled) {
        this.enabled = enabled;
    }

    public void query(long baseMillis) {
        if (!enabled) {
            return;
        }
        double jitter = ThreadLocalRandom.current().nextDouble(0.85, 1.15);
        try {
            Thread.sleep(Math.round(baseMillis * jitter));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
