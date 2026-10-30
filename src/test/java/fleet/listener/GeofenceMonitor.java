package fleet.listener;

import fleet.core.EventPublisher;
import fleet.event.FleetEvent;
import fleet.event.GeofenceAlertEvent;
import fleet.geo.GeoPoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fleet.listener.Events.gps;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeofenceMonitorTest {
    private final EventPublisher<FleetEvent> alerts = new EventPublisher<>(error -> { });
    private final List<GeofenceAlertEvent> received = new ArrayList<>();
    private final GeofenceMonitor monitor =
            new GeofenceMonitor(Map.of("D1", new GeoPoint(51.0, 71.0)), 5.0, alerts);

    GeofenceMonitorTest() {
        alerts.subscribe(GeofenceAlertEvent.class, received::add);
    }

    @Test
    void vehicleInsideRadiusRaisesNoAlert() {
        monitor.onEvent(gps("T1", "D1", 51.0, 71.0, 40));

        assertTrue(received.isEmpty());
    }

    @Test
    void vehicleOutsideRadiusRaisesAlertWithDistance() {
        monitor.onEvent(gps("T1", "D1", 51.1, 71.0, 40));

        assertEquals(1, received.size());
        assertEquals(11.12, received.get(0).distanceKm(), 0.01);
    }

    @Test
    void staysSilentWhileVehicleRemainsOutside() {
        monitor.onEvent(gps("T1", "D1", 51.1, 71.0, 40));
        monitor.onEvent(gps("T1", "D1", 51.2, 71.0, 40));

        assertEquals(1, received.size());
    }

    @Test
    void alertsAgainAfterVehicleCameBackAndLeftAgain() {
        monitor.onEvent(gps("T1", "D1", 51.1, 71.0, 40));
        monitor.onEvent(gps("T1", "D1", 51.0, 71.0, 40));
        monitor.onEvent(gps("T1", "D1", 51.1, 71.0, 40));

        assertEquals(2, received.size());
    }
}