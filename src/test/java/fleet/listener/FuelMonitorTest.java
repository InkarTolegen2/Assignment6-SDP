package fleet.listener;

import fleet.core.EventPublisher;
import fleet.event.FleetEvent;
import fleet.event.LowFuelAlertEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static fleet.listener.Events.fuel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuelMonitorTest {
    private final EventPublisher<FleetEvent> alerts = new EventPublisher<>(error -> { });
    private final List<LowFuelAlertEvent> received = new ArrayList<>();
    private final FuelMonitor monitor = new FuelMonitor(20.0, alerts);

    FuelMonitorTest() {
        alerts.subscribe(LowFuelAlertEvent.class, received::add);
    }

    @Test
    void enoughFuelRaisesNoAlert() {
        monitor.onEvent(fuel("T1", 60));

        assertTrue(received.isEmpty());
    }

    @Test
    void fuelBelowThresholdRaisesAlert() {
        monitor.onEvent(fuel("T1", 12.5));

        assertEquals(1, received.size());
        assertEquals(12.5, received.get(0).liters(), 1e-9);
    }

    @Test
    void fuelExactlyAtThresholdIsStillFine() {
        monitor.onEvent(fuel("T1", 20.0));

        assertTrue(received.isEmpty());
    }
}