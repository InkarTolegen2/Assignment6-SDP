package fleet.integration;

import fleet.core.EventPublisher;
import fleet.event.FleetEvent;
import fleet.event.FuelLevelEvent;
import fleet.event.InspectionReportEvent;
import fleet.event.LowFuelAlertEvent;
import fleet.listener.FuelMonitor;
import fleet.visitor.SampleFleet;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineTest {
    private final EventPublisher<FleetEvent> fuelFeed = new EventPublisher<>(error -> { });
    private final EventPublisher<FleetEvent> alerts = new EventPublisher<>(error -> { });
    private final EventPublisher<FleetEvent> reports = new EventPublisher<>(error -> { });
    private final InspectionLog log = new InspectionLog();

    PipelineTest() {
        fuelFeed.subscribe(FuelLevelEvent.class, new FuelMonitor(20.0, alerts));
        alerts.subscribe(LowFuelAlertEvent.class, new InspectionService(SampleFleet.create(), 8_000, reports));
        reports.subscribe(InspectionReportEvent.class, log);
    }

    @Test
    void lowFuelReadingEndsAsInspectionReport() {
        fuelFeed.publish(new FuelLevelEvent("T2", "D2", 10.0, Instant.EPOCH));

        assertEquals(1, log.reports().size());
        assertEquals("D2", log.reports().get(0).depotId());
        assertEquals(2_725.0, log.reports().get(0).maintenanceCost(), 1e-6);
    }

    @Test
    void normalFuelReadingProducesNoReport() {
        fuelFeed.publish(new FuelLevelEvent("T2", "D2", 50.0, Instant.EPOCH));

        assertTrue(log.reports().isEmpty());
    }

    @Test
    void concurrentAlertsProduceExactlyOneReportEach() throws Exception {
        int threads = 4;
        int alertsPerThread = 250;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                start.await();
                for (int i = 0; i < alertsPerThread; i++) {
                    fuelFeed.publish(new FuelLevelEvent("T1", "D1", 5.0, Instant.EPOCH));
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();

        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(threads * alertsPerThread, log.reports().size());
    }
}