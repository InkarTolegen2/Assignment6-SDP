package fleet.sim;

import fleet.core.EventPublisher;
import fleet.event.FleetEvent;
import fleet.event.FuelLevelEvent;
import fleet.event.GpsPositionEvent;
import fleet.geo.GeoPoint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FleetSimulator {
    public record Result(int gpsEvents, int fuelEvents, boolean stoppedEarly) {}

    private final Map<String, GeoPoint> depots;
    private final List<String> depotIds;
    private final EventPublisher<FleetEvent> gpsFeed;
    private final EventPublisher<FleetEvent> fuelFeed;
    private final long seed;
    private final AtomicBoolean stopRequested = new AtomicBoolean();

    public FleetSimulator(Map<String, GeoPoint> depots, EventPublisher<FleetEvent> gpsFeed,
                          EventPublisher<FleetEvent> fuelFeed, long seed) {
        if (depots.isEmpty()) {
            throw new IllegalArgumentException("at least one depot is needed");
        }
        this.depots = Map.copyOf(depots);
        this.depotIds = depots.keySet().stream().sorted().toList();
        this.gpsFeed = gpsFeed;
        this.fuelFeed = fuelFeed;
        this.seed = seed;
    }

    public void requestStop() {
        stopRequested.set(true);
    }

    public Result run(int producerThreads, int eventsPerProducer) throws InterruptedException {
        if (producerThreads < 1) {
            throw new IllegalArgumentException("at least one producer thread is needed");
        }
        stopRequested.set(false);
        AtomicInteger gpsCount = new AtomicInteger();
        AtomicInteger fuelCount = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(producerThreads, new ProducerThreads());
        try {
            List<Callable<Void>> producers = new ArrayList<>();
            for (int index = 0; index < producerThreads; index++) {
                int producerIndex = index;
                producers.add(() -> {
                    produce(producerIndex, eventsPerProducer, gpsCount, fuelCount);
                    return null;
                });
            }
            for (Future<Void> finished : pool.invokeAll(producers)) {
                finished.get();
            }
        } catch (ExecutionException failure) {
            throw new IllegalStateException("a producer failed", failure.getCause());
        } finally {
            pool.shutdown();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        }
        return new Result(gpsCount.get(), fuelCount.get(), stopRequested.get());
    }

    private void produce(int index, int events, AtomicInteger gpsCount, AtomicInteger fuelCount) {
        String depotId = depotIds.get(index % depotIds.size());
        GeoPoint base = depots.get(depotId);
        String vehicleId = "V" + index;
        Random random = new Random(seed + index);
        for (int n = 0; n < events && !stopRequested.get(); n++) {
            Instant time = Instant.EPOCH.plusSeconds(n);
            gpsFeed.publish(new GpsPositionEvent(vehicleId, depotId,
                    base.latitude() + (random.nextDouble() - 0.5) * 0.2,
                    base.longitude() + (random.nextDouble() - 0.5) * 0.2,
                    random.nextDouble() * 90, time));
            gpsCount.incrementAndGet();
            fuelFeed.publish(new FuelLevelEvent(vehicleId, depotId, 5 + random.nextDouble() * 55, time));
            fuelCount.incrementAndGet();
        }
    }

    /** Gives the producer threads readable names so that a thread leak is easy to see. */
    private static final class ProducerThreads implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable task) {
            return new Thread(task, "fleet-producer-" + counter.getAndIncrement());
        }
    }
}