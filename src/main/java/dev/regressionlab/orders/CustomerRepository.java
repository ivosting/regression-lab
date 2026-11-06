package dev.regressionlab.orders;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

/**
 * In-memory stand-in for the customers table. Each method pays one simulated
 * database round-trip, regardless of how many ids it resolves.
 */
@Repository
public class CustomerRepository {

    private static final String[] NAMES = {"Ana", "Bruno", "Carla", "Diego", "Elisa", "Fabio", "Gabi", "Hugo"};

    private final SimulatedLatency latency;

    public CustomerRepository(SimulatedLatency latency) {
        this.latency = latency;
    }

    /** Batch lookup: one round-trip for all ids (~10 ms). */
    public Map<Long, String> findNamesByIds(Collection<Long> ids) {
        latency.query(10);
        Map<Long, String> result = new LinkedHashMap<>();
        for (Long id : ids) {
            result.put(id, nameOf(id));
        }
        return result;
    }

    /** Single-row lookup: one round-trip per call (~35 ms). */
    public String findNameById(long id) {
        latency.query(35);
        return nameOf(id);
    }

    private static String nameOf(long id) {
        return NAMES[(int) (id % NAMES.length)];
    }
}
