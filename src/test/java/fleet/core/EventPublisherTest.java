package fleet.core;

import fleet.event.FleetEvent;
import fleet.event.FuelLevelEvent;
import fleet.event.GpsPositionEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventPublisherTest {
    private final List<Throwable> errors = new CopyOnWriteArrayList<>();
    private final EventPublisher<FleetEvent> publisher = new EventPublisher<>(errors::add);

    private static GpsPositionEvent gps(String vehicleId) {
        return new GpsPositionEvent(vehicleId, "D1", 51.1, 71.4, 60, Instant.EPOCH);
    }

    private static FuelLevelEvent fuel(String vehicleId) {
        return new FuelLevelEvent(vehicleId, "D1", 40, Instant.EPOCH);
    }

    @Test
    void subscriberReceivesEventOfItsType() {
        List<String> received = new ArrayList<>();
        publisher.subscribe(GpsPositionEvent.class, e -> received.add(e.vehicleId()));

        publisher.publish(gps("T1"));

        assertEquals(List.of("T1"), received);
    }

    @Test
    void subscriberDoesNotReceiveOtherEventTypes() {
        List<String> received = new ArrayList<>();
        publisher.subscribe(GpsPositionEvent.class, e -> received.add(e.vehicleId()));

        publisher.publish(fuel("T1"));

        assertTrue(received.isEmpty());
    }

    @Test
    void allSubscribersReceiveEventInSubscriptionOrder() {
        List<String> order = new ArrayList<>();
        publisher.subscribe(GpsPositionEvent.class, e -> order.add("first"));
        publisher.subscribe(GpsPositionEvent.class, e -> order.add("second"));

        publisher.publish(gps("T1"));

        assertEquals(List.of("first", "second"), order);
    }

    @Test
    void unsubscribedObserverGetsNothingMore() {
        AtomicInteger count = new AtomicInteger();
        Observer<GpsPositionEvent> observer = e -> count.incrementAndGet();
        publisher.subscribe(GpsPositionEvent.class, observer);
        publisher.publish(gps("T1"));

        publisher.unsubscribe(GpsPositionEvent.class, observer);
        publisher.publish(gps("T1"));

        assertEquals(1, count.get());
    }

    @Test
    void subscribingTheSameObserverTwiceDeliversOnce() {
        AtomicInteger count = new AtomicInteger();
        Observer<GpsPositionEvent> observer = e -> count.incrementAndGet();
        publisher.subscribe(GpsPositionEvent.class, observer);
        publisher.subscribe(GpsPositionEvent.class, observer);

        publisher.publish(gps("T1"));

        assertEquals(1, count.get());
    }

    @Test
    void observerCanUnsubscribeItselfInsideItsOwnHandler() {
        AtomicInteger oneShotCalls = new AtomicInteger();
        AtomicInteger otherCalls = new AtomicInteger();
        Observer<GpsPositionEvent> oneShot = new Observer<>() {
            @Override
            public void onEvent(GpsPositionEvent event) {
                oneShotCalls.incrementAndGet();
                publisher.unsubscribe(GpsPositionEvent.class, this);
            }
        };
        publisher.subscribe(GpsPositionEvent.class, oneShot);
        publisher.subscribe(GpsPositionEvent.class, e -> otherCalls.incrementAndGet());

        publisher.publish(gps("T1"));
        publisher.publish(gps("T1"));

        assertEquals(1, oneShotCalls.get());
        assertEquals(2, otherCalls.get());
        assertTrue(errors.isEmpty());
    }

    @Test
    void observerCanSubscribeAnotherObserverInsideHandler() {
        AtomicInteger lateCalls = new AtomicInteger();
        publisher.subscribe(GpsPositionEvent.class, e ->
                publisher.subscribe(GpsPositionEvent.class, late -> lateCalls.incrementAndGet()));

        publisher.publish(gps("T1"));
        publisher.publish(gps("T1"));

        assertEquals(1, lateCalls.get());
        assertTrue(errors.isEmpty());
    }

    @Test
    void failingObserverDoesNotBlockOthersAndErrorIsReported() {
        AtomicInteger healthyCalls = new AtomicInteger();
        publisher.subscribe(GpsPositionEvent.class, e -> {
            throw new IllegalStateException("boom");
        });
        publisher.subscribe(GpsPositionEvent.class, e -> healthyCalls.incrementAndGet());

        publisher.publish(gps("T1"));

        assertEquals(1, healthyCalls.get());
        assertEquals(1, errors.size());
    }

    @Test
    void stableObserverSeesEveryEventWhileOthersJoinAndLeaveConcurrently() throws Exception {
        int threads = 4;
        int eventsPerThread = 1_000;
        AtomicInteger stableCalls = new AtomicInteger();
        publisher.subscribe(GpsPositionEvent.class, e -> stableCalls.incrementAndGet());

        ExecutorService pool = Executors.newFixedThreadPool(threads + 1);
        CountDownLatch start = new CountDownLatch(1);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                start.await();
                for (int i = 0; i < eventsPerThread; i++) {
                    publisher.publish(gps("T1"));
                }
                return null;
            });
        }
        pool.submit(() -> {
            start.await();
            for (int i = 0; i < eventsPerThread; i++) {
                Observer<GpsPositionEvent> visitor = e -> { };
                publisher.subscribe(GpsPositionEvent.class, visitor);
                publisher.unsubscribe(GpsPositionEvent.class, visitor);
            }
            return null;
        });
        start.countDown();
        pool.shutdown();

        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(threads * eventsPerThread, stableCalls.get());
        assertTrue(errors.isEmpty());
    }
}