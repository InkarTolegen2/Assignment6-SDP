package fleet.sim;

import fleet.core.EventPublisher;
import fleet.event.FleetEvent;
import fleet.event.FuelLevelEvent;
import fleet.event.GpsPositionEvent;
import fleet.geo.GeoPoint;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetSimulatorTest {
    private static final Map<String, GeoPoint> DEPOTS =
            Map.of("D1", new GeoPoint(51.0, 71.0), "D2", new GeoPoint(43.2, 76.9));

    private final List<Throwable> errors = new CopyOnWriteArrayList<>();
    private final EventPublisher<FleetEvent> gpsFeed = new EventPublisher<>(errors::add);
    private final EventPublisher<FleetEvent> fuelFeed = new EventPublisher<>(errors::add);
    private final FleetSimulator simulator = new FleetSimulator(DEPOTS, gpsFeed, fuelFeed, 42);

    private static boolean producerThreadsAlive() {
        return Thread.getAllStackTraces().keySet().stream()
                .anyMatch(thread -> thread.getName().startsWith("fleet-producer") && thread.isAlive());
    }

    private static void waitUntilProducerThreadsAreGone() throws InterruptedException {
        for (int i = 0; i < 100 && producerThreadsAlive(); i++) {
            Thread.sleep(10);
        }
    }

    @Test
    void fourProducersPublishingAThousandEventsEachAreAllDelivered() throws Exception {
        AtomicInteger gpsReceived = new AtomicInteger();
        AtomicInteger fuelReceived = new AtomicInteger();
        gpsFeed.subscribe(GpsPositionEvent.class, e -> gpsReceived.incrementAndGet());
        fuelFeed.subscribe(FuelLevelEvent.class, e -> fuelReceived.incrementAndGet());

        FleetSimulator.Result result = simulator.run(4, 1_000);

        assertEquals(4_000, gpsReceived.get());
        assertEquals(4_000, fuelReceived.get());
        assertEquals(4_000, result.gpsEvents());
        assertEquals(4_000, result.fuelEvents());
        assertFalse(result.stoppedEarly());
    }

    @Test
    void numberOfProducerThreadsIsConfigurable() throws Exception {
        AtomicInteger gpsReceived = new AtomicInteger();
        gpsFeed.subscribe(GpsPositionEvent.class, e -> gpsReceived.incrementAndGet());

        assertEquals(300, simulator.run(3, 100).gpsEvents());
        assertEquals(800, simulator.run(8, 100).gpsEvents());
        assertEquals(1_100, gpsReceived.get());
    }

    @Test
    void zeroProducerThreadsIsRejected() {
        boolean rejected = false;
        try {
            simulator.run(0, 10);
        } catch (IllegalArgumentException expected) {
            rejected = true;
        } catch (InterruptedException other) {
            Thread.currentThread().interrupt();
        }
        assertTrue(rejected);
    }

    @Test
    void stopRequestEndsTheRunWithoutLosingAnyPublishedEvent() throws Exception {
        AtomicInteger gpsReceived = new AtomicInteger();
        gpsFeed.subscribe(GpsPositionEvent.class, e -> {
            if (gpsReceived.incrementAndGet() == 500) {
                simulator.requestStop();
            }
        });

        FleetSimulator.Result result = simulator.run(3, 1_000_000);

        assertTrue(result.stoppedEarly());
        assertTrue(result.gpsEvents() < 3_000_000);
        assertEquals(result.gpsEvents(), gpsReceived.get());   // published == delivered
    }

    @Test
    void noProducerThreadStaysAliveAfterTheRun() throws Exception {
        simulator.run(4, 100);

        waitUntilProducerThreadsAreGone();
        assertFalse(producerThreadsAlive());
    }

    @Test
    void failingListenerDoesNotLoseEventsForTheOthers() throws Exception {
        AtomicInteger healthyReceived = new AtomicInteger();
        gpsFeed.subscribe(GpsPositionEvent.class, e -> {
            throw new IllegalStateException("boom");
        });
        gpsFeed.subscribe(GpsPositionEvent.class, e -> healthyReceived.incrementAndGet());

        FleetSimulator.Result result = simulator.run(4, 100);

        assertEquals(400, result.gpsEvents());
        assertEquals(400, healthyReceived.get());
        assertEquals(400, errors.size());
    }

    @Test
    void sameSeedProducesTheSameEventsPerVehicle() throws Exception {
        List<GpsPositionEvent> first = collectGpsEvents();
        List<GpsPositionEvent> second = collectGpsEvents();

        assertEquals(first, second);
    }

    private List<GpsPositionEvent> collectGpsEvents() throws InterruptedException {
        ConcurrentLinkedQueue<GpsPositionEvent> seen = new ConcurrentLinkedQueue<>();
        EventPublisher<FleetEvent> gps = new EventPublisher<>(errors::add);
        EventPublisher<FleetEvent> fuel = new EventPublisher<>(errors::add);
        gps.subscribe(GpsPositionEvent.class, seen::add);
        new FleetSimulator(DEPOTS, gps, fuel, 7).run(3, 50);
        return seen.stream()
                .sorted(Comparator.comparing(GpsPositionEvent::vehicleId)
                        .thenComparing(GpsPositionEvent::occurredAt))
                .toList();
    }
}