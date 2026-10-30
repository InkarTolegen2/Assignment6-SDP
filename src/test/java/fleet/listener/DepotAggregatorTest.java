package fleet.listener;

import fleet.core.EventPublisher;
import fleet.event.DepotSummaryEvent;
import fleet.event.FleetEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static fleet.listener.Events.gps;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepotAggregatorTest {
    private final DepotAggregator aggregator =
            new DepotAggregator(3, new EventPublisher<FleetEvent>(error -> { }));
    private final List<DepotSummaryEvent> summaries = new ArrayList<>();

    DepotAggregatorTest() {
        aggregator.subscribe(DepotSummaryEvent.class, summaries::add);
    }

    @Test
    void publishesNothingBeforeTheWindowIsFull() {
        aggregator.onEvent(gps("T1", "D1", 51.0, 71.0, 60));
        aggregator.onEvent(gps("T2", "D1", 51.0, 71.0, 90));

        assertTrue(summaries.isEmpty());
    }

    @Test
    void publishesAverageSpeedAndDistinctVehiclesWhenWindowIsFull() {
        aggregator.onEvent(gps("T1", "D1", 51.0, 71.0, 60));
        aggregator.onEvent(gps("T2", "D1", 51.0, 71.0, 90));
        aggregator.onEvent(gps("T1", "D1", 51.0, 71.0, 30));

        assertEquals(1, summaries.size());
        assertEquals(60.0, summaries.get(0).averageSpeedKmh(), 1e-9);
        assertEquals(2, summaries.get(0).vehicles());
    }

    @Test
    void windowStartsOverAfterASummary() {
        for (int i = 0; i < 4; i++) {
            aggregator.onEvent(gps("T1", "D1", 51.0, 71.0, 60));
        }

        assertEquals(1, summaries.size());
    }

    @Test
    void depotsAreAggregatedSeparately() {
        aggregator.onEvent(gps("T1", "D1", 51.0, 71.0, 60));
        aggregator.onEvent(gps("T2", "D2", 51.0, 71.0, 60));
        aggregator.onEvent(gps("T3", "D1", 51.0, 71.0, 60));
        aggregator.onEvent(gps("T4", "D2", 51.0, 71.0, 60));
        aggregator.onEvent(gps("T5", "D1", 51.0, 71.0, 60));

        assertEquals(1, summaries.size());
        assertEquals("D1", summaries.get(0).depotId());
    }

    @Test
    void dashboardReceivesSummaryThroughTheChain() {
        DepotDashboard dashboard = new DepotDashboard();
        aggregator.subscribe(DepotSummaryEvent.class, dashboard);

        aggregator.onEvent(gps("T1", "D1", 51.0, 71.0, 60));
        aggregator.onEvent(gps("T2", "D1", 51.0, 71.0, 90));
        aggregator.onEvent(gps("T3", "D1", 51.0, 71.0, 30));

        assertEquals(60.0, dashboard.latest("D1").get().averageSpeedKmh(), 1e-9);
        assertTrue(dashboard.latest("D2").isEmpty());
    }
}