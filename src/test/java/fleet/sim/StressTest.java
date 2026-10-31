package fleet.sim;

import fleet.core.EventPublisher;
import fleet.event.DepotSummaryEvent;
import fleet.event.FleetEvent;
import fleet.event.GpsPositionEvent;
import fleet.geo.GeoPoint;
import fleet.listener.DepotAggregator;
import fleet.listener.FleetMap;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Four producer threads, a thousand events each, real listeners: every count must be exact. */
class StressTest {
    @Test
    void fourThreadsTimesThousandEventsGiveExactCountsEverywhere() throws Exception {
        List<Throwable> errors = new CopyOnWriteArrayList<>();
        EventPublisher<FleetEvent> gpsFeed = new EventPublisher<>(errors::add);
        EventPublisher<FleetEvent> fuelFeed = new EventPublisher<>(errors::add);

        AtomicInteger gpsReceived = new AtomicInteger();
        AtomicInteger summaries = new AtomicInteger();
        FleetMap map = new FleetMap();
        DepotAggregator aggregator = new DepotAggregator(100, new EventPublisher<>(errors::add));
        aggregator.subscribe(DepotSummaryEvent.class, summary -> summaries.incrementAndGet());
        gpsFeed.subscribe(GpsPositionEvent.class, e -> gpsReceived.incrementAndGet());
        gpsFeed.subscribe(GpsPositionEvent.class, map);
        gpsFeed.subscribe(GpsPositionEvent.class, aggregator);

        Map<String, GeoPoint> depots = Map.of("D1", new GeoPoint(51.0, 71.0), "D2", new GeoPoint(43.2, 76.9));
        new FleetSimulator(depots, gpsFeed, fuelFeed, 1).run(4, 1_000);

        assertEquals(4_000, gpsReceived.get());
        assertEquals(4, map.trackedVehicles());
        assertEquals(40, summaries.get());   // 2 depots * 2000 events / window of 100
        assertTrue(errors.isEmpty());
    }
}